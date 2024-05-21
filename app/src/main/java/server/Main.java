package server;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Main {
    public static void main(String[] args) {
        VertxOptions options = new VertxOptions().setBlockedThreadCheckInterval(300_000);
        Vertx vertx = Vertx.vertx(options);

        vertx.deployVerticle(new AppServer(), serverResult -> {
            if (serverResult.succeeded()) {
                System.out.println("AppServer deployed successfully");
            } else {
                serverResult.cause().printStackTrace();
            }
        });
    }
}
