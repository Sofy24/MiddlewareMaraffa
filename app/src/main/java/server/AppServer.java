package server;

import chatModule.ChatController;
import game.service.GameServiceDecorator;
import httpRest.RouterConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import repository.AbstractStatisticManager;
import repository.MongoStatisticManager;
import userModule.UserController;

public class AppServer extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(AppServer.class);
	private static final int PORT = 3003;
	private static final String HOST = "localhost";
	private HttpServer server;
	AbstractStatisticManager mongoStatisticManager = new MongoStatisticManager();

	public AppServer() {
	}

	@Override
	public void start() throws Exception {
		final RouterConfig routerConfig = new RouterConfig(PORT, new GameServiceDecorator(vertx, mongoStatisticManager),
				new UserController(vertx), new ChatController(vertx));
		server = vertx.createHttpServer(createOptions());
		server.requestHandler(routerConfig.configurationRouter(vertx));
		server.listen(res -> {
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
		options.setHost(HOST);
		options.setPort(PORT);
		return options;
	}
}
