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

/*
 * This class is responsible for managing the server of the application. Reading and using the enviroment variables and start the application.
 */
public class AppServer extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppServer.class);
	private final int port = Integer.parseInt(System.getenv().getOrDefault("MIDDLEWARE_PORT", "3003"));
	private HttpServer server;

	AbstractStatisticManager mongoStatisticManager = new MongoStatisticManager(
			System.getenv().getOrDefault("MONGO_USER", "mongo_user"),
			System.getenv().getOrDefault("MONGO_PASSWORD", "mongo_password"),
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
		options.setPort(this.port);
		return options;
	}
}
