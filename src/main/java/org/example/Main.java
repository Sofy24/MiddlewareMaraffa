package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import org.example.game.GameVerticle;
import org.example.repository.AbstractStatisticManager;
import org.example.repository.MongoStatisticManager;

public class Main {
    public static void main(String[] args) {
        VertxOptions options = new VertxOptions().setBlockedThreadCheckInterval(300_000);
        Vertx vertx = Vertx.vertx(options);
        // Deploy the main verticle
        // vertx.deployVerticle(new MainVerticle(vertx, mongoStatisticManager));

       vertx.deployVerticle(new AppServer(), serverResult -> {
            if (serverResult.succeeded()) {
                System.out.println("AppServer deployed successfully");
            } else {
                serverResult.cause().printStackTrace();
            }
        });
    }
}
