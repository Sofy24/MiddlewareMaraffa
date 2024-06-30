package chatModule;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import game.GameVerticle;
import game.service.User;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import server.AbstractRestAPI;
import server.WebSocketVertx;

/**
 * TODO javadoc
 */
public class ChatService extends AbstractRestAPI {
	private final Vertx vertx;
	// private static final int PORT = 3004;
	// private static final String LOCALHOST = "127.0.0.1";
	private static final String FE_HOST = "127.0.0.1:80";
	private static int port = Integer.parseInt(Dotenv.load().get("CHAT_PORT", "3004"));
	private static String host = Dotenv.load().get("CHAT_HOST", "localhost");
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatService.class);
	private final WebSocketVertx webSocketVertx;
	private final Map<UUID, GameVerticle> gamesMap;

	public ChatService(final Vertx vertx, final WebSocketVertx WebSocketVertx, final Map<UUID, GameVerticle> map) {
		super(vertx, port, host);
		this.vertx = vertx;
		this.webSocketVertx = WebSocketVertx;
		this.gamesMap = map;
		this.vertx.eventBus().consumer("chat-component:onCreateGame", message -> {
			LOGGER.info("Received message: " + message.body());
			final JsonObject bodyMsg = new JsonObject(message.body().toString());
			// TODO valuta se ha senso mettere .join sulle funzioni che ritornano un
			// CompletableFuture
			this.createGameChatHandler(bodyMsg.getString("gameID"));
			message.reply("pong!");
		});

		this.vertx.eventBus().consumer("chat-component:onJoinGame", message -> {
			LOGGER.info("Received message: " + message.body());
			final JsonObject bodyMsg = new JsonObject(message.body().toString());
			this.joinGameHandler(bodyMsg.getString("gameID"),
					new User(bodyMsg.getString("userID"), UUID.fromString(bodyMsg.getString("clientID")), false));
			message.reply("pong!");
		});
	}

	public CompletableFuture<JsonObject> joinGameHandler(final String gameID, final User user) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		this.askServiceWithFuture(new JsonObject().put("callback", FE_HOST + "/manageMessage/" + user.clientID()), // enorme
				// assunzione
				HttpMethod.POST, "/joinChat/" + gameID + "/" + user.username(), future)
				.whenComplete((result, error) -> {
					if (result.containsKey("error")) {
						LOGGER.error("Error in joining game : " + result.getString("error"));
					} else {
						LOGGER.info("Game joined");
					}
				});
		return future;
	}

	public CompletableFuture<JsonObject> createGameChatHandler(final String gameID) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		this.askServiceWithFutureNoBody(HttpMethod.POST, "/chat/create/:" + gameID, future);
		future.whenComplete((result, error) -> {
			if (result.containsKey("error")) {
				LOGGER.error("Error in creating chat : " + result.getString("error"));
			} else {
				LOGGER.info("Chat created");
			}
		});
		return future;
	}

	public void notificationReceived(final String msg, final Optional<UUID> gameID) {
		LOGGER.info("Received notification: " + msg);
		final JsonObject message = new JsonObject()
				.put("event", "notification")
				.put("message", msg);
		if (gameID.isPresent()) {
			this.gamesMap.get(gameID.get()).getUsers().forEach(user -> {
				this.webSocketVertx.sendMessageToClient(user.clientID(), message.toString());
			});
		} else {
			this.webSocketVertx.broadcastToEveryone(message.toString());
		}
	}

	public void messageReceived(final String msg, final Optional<UUID> gameID, final String author) {
		LOGGER.info("Received message: " + msg);
		System.out.println(msg);
		final JsonObject message = new JsonObject()
				.put("event", "message")
				.put("author", author).put("message", msg);
		if (gameID.isPresent()) {
			this.gamesMap.get(gameID.get()).getUsers().forEach(user -> {
				this.webSocketVertx.sendMessageToClient(user.clientID(), message.toString());
			});
		} else {
			this.webSocketVertx.broadcastToEveryone(message.toString());
		}
	}
}
