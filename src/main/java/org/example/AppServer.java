package org.example;


import org.example.httpRest.RouterConfig;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.example.repository.AbstractStatisticManager;
import org.example.repository.MongoStatisticManager;
import org.example.service.GameServiceDecorator;

public class AppServer extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppServer.class);
  private static final int PORT = 8080;
  private static final String HOST = "localhost";
  private HttpServer server;
  AbstractStatisticManager mongoStatisticManager = new MongoStatisticManager();

  public AppServer() {

  }

  @Override
  public void start() throws Exception {
    RouterConfig routerConfig = new RouterConfig(PORT, new GameServiceDecorator(vertx, null));//new RouterConfig(PORT, new GameServiceDecorator(vertx, mongoStatisticManager));
    server = vertx.createHttpServer(createOptions());
    server.requestHandler(routerConfig.configurationRouter(vertx));
    server.listen(res -> {
      {
        if (res.succeeded()) {
          LOGGER.info("Server is now listening!");
          System.out.println("Server is now listening!");
        } else {
          LOGGER.error("Failed to bind!");
          System.out.println("Failed to bind!");
        }
      }
    });
  }

  private HttpServerOptions createOptions() {
    HttpServerOptions options = new HttpServerOptions();
    options.setHost(HOST);
    options.setPort(PORT);
    return options;
  }

}
