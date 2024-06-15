package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chatModule.ChatController;
import game.service.GameServiceDecorator;
import httpRest.RouterConfig;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import repository.AbstractStatisticManager;
import repository.MongoStatisticManager;
import userModule.UserController;

public class AppServer extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppServer.class);
	private final int port = Integer.parseInt(Dotenv.load().get("MIDDLEWARE_PORT", "3003"));
	private final String host = Dotenv.load().get("MIDDLEWARE_HOST", "localhost");
	private HttpServer server;
	AbstractStatisticManager mongoStatisticManager = new MongoStatisticManager();

	public AppServer() {
	}

	@Override
	public void start() throws Exception {
		final WebSocketVertx webSocket = new WebSocketVertx();
		final RouterConfig routerConfig = new RouterConfig(this.port,
				new GameServiceDecorator(this.vertx, this.mongoStatisticManager, webSocket),
				new UserController(this.vertx),
				new ChatController(this.vertx, webSocket));
		this.server = this.vertx.createHttpServer(this.createOptions());
		this.server.webSocketHandler(webSocket::handleWebSocket);
		this.server.requestHandler(routerConfig.configurationRouter(this.vertx));
		this.server.listen(res -> {
			{
				if (res.succeeded()) {
					LOGGER.info("Server is now listening!");
				} else {
					LOGGER.error("Failed to bind!");
				}
			}
		});
	}

	private HttpServerOptions createOptions() {
		final HttpServerOptions options = new HttpServerOptions();
		options.setHost(this.host);
		options.setPort(this.port);
		return options;
	}
}
