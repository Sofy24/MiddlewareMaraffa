package org.example;

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.parser.Entity;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.example.httpRest.RouterConfig;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import generator.OpenApiRoutePublisher;
import generator.Required;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class AppServer extends AbstractVerticle {
  private static final Logger LOGGER = LoggerFactory.getLogger(AppServer.class);

  public static final String APPLICATION_JSON = "application/json";
  private static final int PORT = 8080;
  private static final String HOST = "localhost";

  private HttpServer server;

  public AppServer() {

  }

  @Override
  public void start() throws Exception {
    // RouterConfig routerConfig = new RouterConfig(PORT, new EntityService(vertx));
    RouterConfig routerConfig = new RouterConfig(PORT);

    server = vertx.createHttpServer(createOptions());
    server.requestHandler(routerConfig.configurationRouter());
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
