package server;

import java.util.concurrent.CompletableFuture;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public abstract class AbstractRestAPI {
    private final Vertx vertx;
    private final int port;
    private final String localhost;

    public AbstractRestAPI(final Vertx vertx, final int port, final String localhost) {
        this.vertx = vertx;
        this.port = port;
        this.localhost = localhost;
    }

    public CompletableFuture<JsonObject> askService(final JsonObject requestBody, final HttpMethod method,
            final String requestURI) {
        final CompletableFuture<JsonObject> future = new CompletableFuture<>();
        WebClient.create(this.vertx).request(method, this.port, this.localhost, requestURI)
                .putHeader("Accept", "application/json").as(BodyCodec.jsonObject())
                .sendJsonObject(requestBody, handler -> {
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
        WebClient.create(this.vertx).request(method, this.port, this.localhost, requestURI)
                .putHeader("Accept", "application/json").as(BodyCodec.jsonObject())
                .sendJsonObject(requestBody, handler -> {
                    if (handler.succeeded()) {
                        future.complete(handler.result().body());
                    } else {
                        future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
                    }
                });
        return future;
    }

    public CompletableFuture<JsonObject> askServiceWithFutureNoBody(final HttpMethod method, final String requestURI,
            final CompletableFuture<JsonObject> future) {
        WebClient.create(this.vertx).request(method, this.port, this.localhost, requestURI)
                .putHeader("Accept", "application/json").as(BodyCodec.jsonObject()).send(handler -> {
                    if (handler.succeeded()) {
                        future.complete(handler.result().body());
                    } else {
                        future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
                    }
                });
        return future;
    }
}
