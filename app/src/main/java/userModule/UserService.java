package userModule;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

import game.GameVerticle;
import game.Trick;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
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
            Gson gson = new Gson();
            //! Esiste un metodo toJson in cui metti solo alcune chiavi, vale la pena usarlo !
            JsonObject jj = new JsonObject(message.body().toString());
            // GameVerticle gv = gson.fromJson(message.body().toString() , GameVerticle.class);
            // System.out.println("users: " + gv.getUsers());
            System.out.println(" \t\t\t\t\t Points: " + jj.getString("points"));
            // LOGGER.debug(gv.toString());
            // System.out.println(gv.toString());
            message.reply("pong!");
        });
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


}
