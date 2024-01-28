package org.example;

import io.vertx.core.Vertx;
import org.example.game.GameVerticle;
import org.example.repository.AbstractStatisticManager;
import org.example.repository.MongoStatisticManager;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        AbstractStatisticManager mongoStatisticManager = new MongoStatisticManager();
        // Deploy the main verticle
        vertx.deployVerticle(new MainVerticle(vertx, mongoStatisticManager));
    }
}
