package org.example;

import io.vertx.core.Vertx;
import org.example.game.GameVerticle;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // Deploy the main verticle
        vertx.deployVerticle(new MainVerticle(vertx));
    }
}
