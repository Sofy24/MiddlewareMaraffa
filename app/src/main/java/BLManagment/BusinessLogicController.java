package BLManagment;

import game.Trick;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import java.util.concurrent.CompletableFuture;

public class BusinessLogicController {
	private final Vertx vertx;
	private static final int PORT = 3000;
	private static final String LOCALHOST = "127.0.0.1";
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessLogicController.class);

	public BusinessLogicController(final Vertx vertx) {
		this.vertx = vertx;
	}

	public CompletableFuture<JsonObject> getShuffledDeck(final Integer numberOfPlayers) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		System.out.println("Getting the shuffled deck");
		WebClient.create(vertx).get(PORT, LOCALHOST, "/games/startRound")
				// .ssl(true)
				.putHeader("Accept", "application/json") // (4)
				.as(BodyCodec.jsonObject()).send(handler -> {
					System.out.println("Got any response ??");
					if (handler.succeeded()) {
						future.complete(handler.result().body());
					} else {
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

	/**
	 * perform a post request to business logic in order to compute the score, get
	 * the winning team and position
	 *
	 * @param trick
	 *            the completed trick with which it computes the score
	 * @param trump
	 *            used while computing the score
	 * @return a completable future of the json response
	 */
	public CompletableFuture<JsonObject> computeScore(final Trick trick, final String trump) {
		final int[] cards = trick.getCards().stream().mapToInt(card -> Integer.parseInt(card)).toArray();
		final JsonObject requestBody = new JsonObject().put("trick", cards).put("trump", Integer.parseInt(trump));
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		LOGGER.info("Computing the score");
		WebClient.create(vertx).post(PORT, LOCALHOST, "/games/computeScore").putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).sendJsonObject(requestBody, handler -> {
					if (handler.succeeded()) {
						future.complete(handler.result().body());
					} else {
						LOGGER.info("Error in computing the score");
						LOGGER.error("Error in computing the score");
						future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});
		return future;
	}
}
