package org.example.httpRest;

import java.util.concurrent.CompletableFuture;

import org.example.game.Trick;


import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.rxjava.core.Future;
import rx.Completable;

public class BusinessLogicController {
    private Vertx vertx;
    private static final int PORT = 3000;
    private static final String LOCALHOST = "127.0.0.1";
    private final Router router;
    // private WebClient webClient;

    public BusinessLogicController(Vertx vertx) {
        this.vertx = vertx;
        this.router = Router.router(vertx);
        // this.webClient = WebClient.create(vertx);
        // setup();
    }

    /*
     * private void setup(){
     * router.post("/computeScore").handler(this::computeScore);
     * vertx.createHttpServer()
     * .requestHandler(router)
     * .listen(8080, http -> {
     * if (http.succeeded()) {
     * //startFuture.complete();
     * System.out.println("HTTP server started on port 8080");
     * } else {
     * //startFuture.fail(http.cause());
     * System.out.println("erorr");
     * }
     * });
     * }
     */
    public CompletableFuture<JsonObject> getShuffledDeck(final Integer numberOfPlayers) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        System.out.println("Getting the shuffled deck");
        // WebClient.create(vertx).get(443,
        // "icanhazdadjoke.com",
        // "/") // (2)
        WebClient.create(vertx).get(PORT, LOCALHOST, "/games/startRound")
                // .ssl(true)
                .putHeader("Accept",
                        "application/json") // (4)
                .as(BodyCodec.jsonObject())
                .send(handler -> {
                    System.out.println("Got any response ??");
                    if (handler.succeeded()) {
                        future.complete(handler.result().body());
                    } else {
                        // future.complete(handler.cause().getMessage());
                        System.out.println("Error in getting the shuffled deck");
                        System.out.println(handler.cause().getMessage());
                        future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
                    }
                });
        // .onFailure(
        // fail -> {
        // System.out.println("Error in getting the shuffled deck");
        // System.out.println(fail.getMessage());
        // future.completeExceptionally(fail);
        // }
        // )
        // .onSuccess(response -> {
        // System.out.println("Got the shuffled deck");
        // if (response.statusCode() == 200) {
        // JsonObject json = response.bodyAsJsonObject();
        // System.out.println(json);
        // future.complete(json);
        // } else {
        // System.out.println("Error in getting the shuffled deck");
        // }
        // });
        return future;
    }

    public CompletableFuture<JsonObject> computeScore(Trick trick, String trump) {
        JsonObject requestBody = new JsonObject()
                .put("trick", new int[]{1, 2, 3, 4})
                .put("trump", 2);
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        System.out.println("Computing the score");
        WebClient.create(vertx).post(PORT, LOCALHOST, "/games/computeScore")
                .putHeader("Accept", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(requestBody, handler -> {
                    if (handler.succeeded()) {
                        future.complete(handler.result().body());
                    } else {
                        System.out.println("Error in computing the score");
                        System.out.println(handler.cause().getMessage());
                        future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
                    }
                });
        return future;
    }
}
