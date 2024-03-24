package userModule;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

import game.GameVerticle;
import game.Team;
import game.Trick;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class UserService {
    private Vertx vertx;
    private static final int PORT = 3001;
    private static final String LOCALHOST = "127.0.0.1";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class); 
    
    public UserService(Vertx vertx) {
        this.vertx = vertx;
        this.vertx.eventBus().consumer("user-component", message -> {
            LOGGER.info("Received message: " + message.body());
            //! Esiste un metodo toJson in cui metti solo alcune chiavi, vale la pena usarlo !
            JsonObject jj = new JsonObject(message.body().toString());
            // GameVerticle gv = gson.fromJson(message.body().toString() , GameVerticle.class);
            // System.out.println("users: " + gv.getUsers());
            // System.out.println(" \t\t\t\t\t Points: " + jj.getString("points"));
            // LOGGER.debug(gv.toString());
            // System.out.println(gv.toString());
            this.endGameHandler(new JsonObject(message.body().toString()));
            message.reply("pong!");
        });
    }

    public CompletableFuture<Boolean> endGameHandler(JsonObject requestBody) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        //TODO devi creare una chiamata per modificare le stats altrimenti e' davvero scomodo lavorare
        Team t1 = (Team) requestBody.getValue("team1", Team.class);
        Team t2 = (Team) requestBody.getValue("team2", Team.class);
        JsonArray updates = new JsonArray();
        t1.players().forEach(team1Player -> {
            updates.add(new JsonObject().put("nickname", team1Player).put("win", t1.score() > t2.score()).put("cricca", 0)); //TODO la criccaaaaa 
        });
        t2.players().forEach(team2Player -> {
            updates.add(new JsonObject().put("nickname", team2Player).put("win", t2.score() > t1.score()).put("cricca", 0)); //TODO la criccaaaaa 
        });

        // WebClient.create(vertx)
        //     .request(HttpMethod.GET, PORT, LOCALHOST, "/user/user1")
        //         .putHeader("Accept", "application/json")
        //         .putHeader("Content-type", "application/json")
        //         .as(BodyCodec.jsonObject())
        //         .send(handler -> {
        //             if (handler.succeeded()) {
        //                 System.out.println(handler.result().toString());
        //                 System.out.println(handler.result().body());
        //                 future.complete(true);
        //             } else {
        //                 System.out.println(handler.cause().getMessage());
        //                 future.complete(false);
        //             }
        //         }); //TODO test after login and register

        WebClient.create(vertx)
            .request(HttpMethod.POST, PORT, LOCALHOST, "/statistic/bulk")
                .putHeader("Accept", "application/json")
                .putHeader("Content-type", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJson(updates, handler -> {
                    if (handler.succeeded() && handler.result().statusCode() == 200){ 
                        // System.out.println(handler.result().body().toString());

                        future.complete(true);
                        // future.complete(handler.result().body());
                    } else {
                        System.out.println(handler.cause().getMessage());
                        future.complete(false);
                        // future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
                    }
                }); //TODO test after login and register
        return future;
    }

    public CompletableFuture<JsonObject> askService(JsonObject requestBody, HttpMethod method, String requestURI) {
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        WebClient.create(vertx)
            .request(method, PORT, LOCALHOST, requestURI)
                .putHeader("Accept", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(requestBody, handler -> {
                    if (handler.succeeded()) {
                        future.complete(handler.result().body());
                    } else {
                        future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
                    }
                });
        return future;
    } 

    public CompletableFuture<JsonObject> askServiceWithFuture(JsonObject requestBody, HttpMethod method, String requestURI, CompletableFuture<JsonObject> future) {
        WebClient.create(vertx)
            .request(method, PORT, LOCALHOST, requestURI)
                .putHeader("Accept", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(requestBody, handler -> {
                    if (handler.succeeded()) {
                        future.complete(handler.result().body());
                    } else {
                        future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
                    }
                });
        return future;
    } 

    public CompletableFuture<JsonObject> registerUser(String nickname, String password, String email) throws InterruptedException, ExecutionException {
        JsonObject requestBody = new JsonObject()
            .put("nickname", nickname)
            .put("password", password)
            .put("email", email);
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        WebClient.create(vertx)
            .request(HttpMethod.GET, PORT, LOCALHOST, "/user/" + nickname)
                .putHeader("Accept", "application/json")
                .as(BodyCodec.jsonObject())
                .send(handler -> {
                    if (handler.succeeded()) {
                        if(handler.result().body() == null) this.askServiceWithFuture(requestBody, HttpMethod.POST, "/user", future);
                        else if(handler.result().body().getString("nickname").equals(nickname)) throw new RuntimeException("User already exists");
                    }
                });
        return future;
                // if(res){
                //     return this.askService(requestBody, HttpMethod.POST, "/user");
                // }else {
                //     throw new RuntimeException("User already exists");
                // }
        // .whenComplete((response, error) -> {
        //     if (error != null) {
        //         LOGGER.error(error.getMessage());
        //     } else {
        //         LOGGER.info(response.encode());
        //     }
        // });
    }
}
