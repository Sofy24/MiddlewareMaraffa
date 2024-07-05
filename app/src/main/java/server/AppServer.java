package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chatModule.ChatController;
import game.service.GameServiceDecorator;
import httpRest.RouterConfig;
// import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import repository.AbstractStatisticManager;
import repository.MongoStatisticManager;
import userModule.UserController;

public class AppServer extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppServer.class);
	private final int port = Integer.parseInt(System.getenv().getOrDefault("MIDDLEWARE_PORT", "3003"));
	private HttpServer server;
	// private final String host = Dotenv.load().get("MIDDLEWARE_HOST",
	// "localhost");
	// AbstractStatisticManager mongoStatisticManager = new MongoStatisticManager(
	// Dotenv.load().get("MONGO_USER", "user"),
	// Dotenv.load().get("MONGO_PASSWORD", "password"),
	// Dotenv.load().get("MONGO_HOST", "localhost"),
	// Integer.parseInt(Dotenv.load().get("MONGO_PORT", "27127")),
	// Dotenv.load().get("MONGO_DATABASE", "maraffa")
	// );
	// "localhost");
	AbstractStatisticManager mongoStatisticManager = new MongoStatisticManager(
			System.getenv().getOrDefault("MONGO_USER", "your_mongo_user"),
			System.getenv().getOrDefault("MONGO_PASSWORD", "your_mongo_password"),
			System.getenv().getOrDefault("MONGO_HOST", "127.0.0.1"),
			Integer.parseInt(System.getenv().getOrDefault("MONGO_PORT", "27012")),
			System.getenv().getOrDefault("MONGO_DATABASE", "MaraffaStatisticsDB"));

	public AppServer() {
	}

	@Override
	public void start() throws Exception {
		final WebSocketVertx webSocket = new WebSocketVertx();
		final GameServiceDecorator gameServiceDecorator = new GameServiceDecorator(this.vertx,
				this.mongoStatisticManager, webSocket);
		final RouterConfig routerConfig = new RouterConfig(this.port,
				gameServiceDecorator,
				new UserController(this.vertx),
				new ChatController(this.vertx, webSocket, gameServiceDecorator));
		this.server = this.vertx.createHttpServer(this.createOptions());
		this.server.webSocketHandler(webSocket::handleWebSocket);
		this.server.requestHandler(routerConfig.configurationRouter(this.vertx));
		this.server.listen(res -> {
			{
				if (res.succeeded()) {
					LOGGER.info("Server is now listening!");
					LOGGER.info("PORT: " + this.port);
				} else {
					LOGGER.error("Failed to bind!");
				}
			}
		});
	}

	private HttpServerOptions createOptions() {
		final HttpServerOptions options = new HttpServerOptions();
		// options.setHost(this.host);
		options.setPort(this.port);
		return options;
	}
}
