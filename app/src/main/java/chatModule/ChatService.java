package chatModule;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class ChatService {
	private final Vertx vertx;
	private static final int PORT = 3004;
	private static final String LOCALHOST = "127.0.0.1";
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);

	public ChatService(final Vertx vertx) {
		this.vertx = vertx;

		this.vertx.eventBus().consumer("chat-component:onCreateGame", message -> {
			LOGGER.info("Received message: " + message.body());
			final JsonObject bodyMsg = new JsonObject(message.body().toString());
			this.createGameChatHandler(bodyMsg.getString("gameID"));
			message.reply("pong!");
		});

		this.vertx.eventBus().consumer("chat-component:onJoinGame", message -> {
			LOGGER.info("Received message: " + message.body());
			message.reply("pong!");
		});
	}

	public CompletableFuture<JsonObject> createGameChatHandler(final String gameID) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		this.askServiceWithFutureNoBody(HttpMethod.POST, "/chat/create/:" + gameID, future);
		future.whenComplete((result, error) -> {
			if (error != null) {
				LOGGER.error("Error in creating chat", error);
			} else {
				LOGGER.info("Chat created");
			}
		});
		return future;
	}

	public void messageReceived(final String msg) {
		LOGGER.info("Received message: " + msg);
		System.out.println(msg);
	}

	public CompletableFuture<JsonObject> askServiceWithFutureNoBody(final HttpMethod method,
			final String requestURI, final CompletableFuture<JsonObject> future) {
		WebClient.create(this.vertx).request(method, PORT, LOCALHOST, requestURI)
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).send(handler -> {
					if (handler.succeeded()) {
						future.complete(handler.result().body());
					} else {
						future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});
		return future;
	}

	public CompletableFuture<JsonObject> askServiceWithFuture(final JsonObject requestBody, final HttpMethod method,
			final String requestURI, final CompletableFuture<JsonObject> future) {
		WebClient.create(this.vertx).request(method, PORT, LOCALHOST, requestURI)
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).sendJsonObject(requestBody, handler -> {
					if (handler.succeeded()) {
						future.complete(handler.result().body());
					} else {
						future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});
		return future;
	}
}
