package userModule;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;

import game.Team;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import server.AbstractRestAPI;

public class UserService extends AbstractRestAPI {
	private final Vertx vertx;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	public UserService(final Vertx vertx, final String host, final Integer port) {
		super(vertx, port, host);
		this.vertx = vertx;
		this.vertx.eventBus().consumer("user-component", message -> {
			LOGGER.info("Received message: " + message.body());
			this.endGameHandler(new JsonObject(message.body().toString()));
			message.reply("pong!");
		});
	}

	public CompletableFuture<Boolean> endGameHandler(final JsonObject requestBody) {
		final CompletableFuture<Boolean> future = new CompletableFuture<>();
		final Gson gg = new Gson();
		final Team t1 = (Team) gg.fromJson(requestBody.getValue("teamA").toString(), Team.class);
		final Team t2 = (Team) gg.fromJson(requestBody.getValue("teamB").toString(), Team.class);

		final JsonArray updates = new JsonArray();
		t1.players()
				.stream()
				.filter(user -> !user.guest())
				.forEach(team1Player -> {
					updates.add(
							new JsonObject().put("nickname", team1Player).put("win", t1.score() > t2.score())
									.put("cricca", 0)); // TODO
					// la
					// criccaaaaa
				});
		t2.players()
				.stream()
				.filter(user -> !user.guest())
				.forEach(team2Player -> {
					updates.add(
							new JsonObject().put("nickname", team2Player).put("win", t2.score() > t1.score())
									.put("cricca", 0)); // TODO
					// la
					// criccaaaaa
				});

		WebClient.create(this.vertx).request(HttpMethod.POST, this.getPort(), this.getHost(), "/statistic/bulk")
				.putHeader("Accept", "application/json").putHeader("Content-type", "application/json")
				.as(BodyCodec.jsonObject()).sendJson(updates, handler -> {
					if (handler.succeeded() && handler.result().statusCode() == 200) {
						// System.out.println(handler.result().body().toString());
						future.complete(true);
						// future.complete(handler.result().body());
					} else {
						System.out.println(handler.cause().getMessage());
						future.complete(false);
						// future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});
		return future;
	}

	public CompletableFuture<JsonObject> getUserInfo(final String nickname) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		this.askServiceWithFutureNoBody(HttpMethod.GET, "/user/" + nickname, future);
		return future;
	}

	public CompletableFuture<JsonObject> registerUser(final String nickname, final String password,
			final String email) {
		final JsonObject requestBody = new JsonObject().put("nickname", nickname).put("password", password).put("email",
				email);
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		WebClient.create(this.vertx).request(HttpMethod.GET, this.getPort(), this.getHost(), "/user/" + nickname)
				.putHeader("Accept", "application/json").as(BodyCodec.jsonObject()).send(handler -> {
					if (handler.succeeded()) {
						if (handler.result().statusCode() == 404)
							this.askServiceWithFuture(requestBody, HttpMethod.POST, "/user", future);
						else if (handler.result().body().getString("nickname").equals(nickname))
							future.complete(new JsonObject().put("status", "409").put("error", "User already exists"));
						// future.completeExceptionally(new RuntimeException("User already exists"));
					}
				});
		return future;
	}

	public CompletableFuture<JsonObject> loginUser(final String nickname, final String password) {
		final JsonObject requestBody = new JsonObject().put("nickname", nickname).put("password", password);
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		System.out.println("Calling login user at " + this.getHost() + ":" + this.getPort());
		WebClient.create(this.vertx).request(HttpMethod.POST, this.getPort(), this.getHost(), "/login")
				.putHeader("Content-type", "application/json").putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).sendJsonObject(requestBody, handler -> {
					if (handler.succeeded()) {
						System.out.println("Login response: " + handler.result().body().toString());
						if (handler.result().statusCode() == 200)
							future.complete(JsonObject.of().put("status", String.valueOf(handler.result().statusCode()))
									.put("token", encryptThisString(nickname)));
						else
							future.complete(
									new JsonObject().put("status", String.valueOf(handler.result().statusCode()))
											.put("error", handler.result().body().getString("message")));
					}
				});
		return future;
	}

	public CompletableFuture<JsonObject> resetUserPassword(final String nickname, final String password) {
		final JsonObject requestBody = new JsonObject().put("nickname", nickname).put("password", password);
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		System.out.println("Calling login user at " + this.getHost() + ":" + this.getPort());
		WebClient.create(this.vertx).request(HttpMethod.POST, this.getPort(), this.getHost(), "/reset-password")
				.putHeader("Content-type", "application/json").putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).sendJsonObject(requestBody, handler -> {
					if (handler.succeeded()) {
						System.out.println("Login response: " + handler.result().body().toString());
						if (handler.result().statusCode() == 200)
							future.complete(JsonObject.of().put("status", String.valueOf(handler.result().statusCode()))
									.put("token", encryptThisString(nickname)));
						else
							future.complete(
									new JsonObject().put("status", String.valueOf(handler.result().statusCode()))
											.put("error", handler.result().body().getString("message")));
					}
				});
		return future;
	}

	private static String encryptThisString(final String input) {
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-512");
			final byte[] messageDigest = md.digest(input.getBytes());
			final BigInteger no = new BigInteger(1, messageDigest);
			String hashtext = no.toString(16);
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public CompletableFuture<JsonObject> logOutUser(final String nickname) {
		final JsonObject requestBody = new JsonObject().put("nickname", nickname);
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		System.out.println("Calling login user at " + this.getHost() + ":" + this.getPort());
		WebClient.create(this.vertx).request(HttpMethod.POST, this.getPort(), this.getHost(), "/logout")
				.putHeader("Content-type", "application/json").putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).sendJsonObject(requestBody, handler -> {
					if (handler.succeeded()) {
						System.out.println("Login response: " + handler.result().body().toString());
						if (handler.result().statusCode() == 200)
							future.complete(
									JsonObject.of().put("status", String.valueOf(handler.result().statusCode())));
						else
							future.complete(
									new JsonObject().put("status", String.valueOf(handler.result().statusCode()))
											.put("error", handler.result().body().getString("message")));
					}
				});
		return future;
	}

}
