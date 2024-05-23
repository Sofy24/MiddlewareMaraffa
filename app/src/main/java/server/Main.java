package server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(final String[] args) {
		final VertxOptions options = new VertxOptions().setBlockedThreadCheckInterval(300_000);
		final Vertx vertx = Vertx.vertx(options);

		vertx.deployVerticle(new AppServer(), serverResult -> {
			if (serverResult.succeeded()) {
				LOGGER.debug("AppServer deployed successfully");
				LOGGER.info("AppServer deployed successfully");
				System.out.println("AppServer deployed successfully");
			} else {
				serverResult.cause().printStackTrace();
			}
		});
	}
}
