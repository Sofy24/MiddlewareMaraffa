package BLManagment;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import com.google.common.primitives.Booleans;
import game.Trick;
import game.service.GameService;
import game.utils.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;


public class BusinessLogicController {
	private final Vertx vertx;
	private static final int PORT = 3000;
	private static final String LOCALHOST = "127.0.0.1";
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessLogicController.class);
	private final GameService gameService;

	public BusinessLogicController(final Vertx vertx, final GameService gameService) {
		this.vertx = vertx;
		this.gameService = gameService;
		this.startRound();
		this.trickCompleted();

	}

	/**
	 * @param numberOfPlayers
	 * @return
	 */
	public CompletableFuture<JsonObject> getShuffledDeck(final UUID gameID, final Integer numberOfPlayers) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		final JsonObject startResponse = new JsonObject();
		WebClient.create(this.vertx).get(PORT, LOCALHOST, "/games/startRound")
				.putHeader("Accept", "application/json") 
				.as(BodyCodec.jsonObject()).send(handler -> {
					if (handler.succeeded()) {
						final JsonArray deck = handler.result().body().getJsonArray("deck");
						final Integer firstPlayer = handler.result().body().getInteger("firstPlayer");
						startResponse.put("deck", deck);
						startResponse.put("firstPlayer", firstPlayer);
						LOGGER.info("The first player is: " + firstPlayer);
						startResponse.put(Constants.START_ATTR, true);
						this.gameService.getGames().get(gameID).setInitialTurn(firstPlayer);
						LOGGER.info("Round started");
						future.complete(handler.result().body());
					} else {
						LOGGER.error("Error in getting the shuffled deck " + handler.cause().getMessage());
						future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});

		return future;
	}

	/* start a new round 
	 * @param gameID the game ID for which to
	 * @return a json object with the deck and the first player if there weren't any errors*/
	public void startRound(){
		this.vertx.eventBus().consumer("game-startRound:onStartGame", message -> {
			final JsonObject startResponse = new JsonObject();
            LOGGER.info("Received message: " + message.body());
			final JsonObject body = new JsonObject((String) message.body());
			final UUID gameID = UUID.fromString(body.getString("gameID"));
        	final int numberOfPlayers = body.getInteger("numberOfPlayers");
            this.getShuffledDeck(gameID, numberOfPlayers).whenComplete((result, error) -> {
				if (!result.containsKey("error")) {
					final JsonArray deck = result.getJsonArray("deck");
					final Integer firstPlayer = result.getInteger("firstPlayer");
					startResponse.put("deck", deck);
					startResponse.put("firstPlayer", firstPlayer);
					LOGGER.info("The first player is: " + firstPlayer);
					startResponse.put(Constants.START_ATTR, true);
					this.gameService.getGames().get(gameID).setInitialTurn(firstPlayer);
				}
                if (error != null) {
                    LOGGER.error("Error when starting the round");
                    message.fail(417, "Error when starting the round");
                } else {
                    LOGGER.info("Round started");
                    message.reply(startResponse);
                }
            });;
        });
		
	}

	/**
	 * perform all the actions the system needs to do when a trick is completed. There's also post request to 
	 * business logic in order to compute the score, get the winning team and position
	 *
	 */
	public void trickCompleted() {
		this.vertx.eventBus().consumer("game-trickCommpleted:onTrickCommpleted", message -> {
			LOGGER.info("Received message: " + message.body());
			final JsonObject body = new JsonObject(message.body().toString());
			final UUID gameID = UUID.fromString(body.getString(Constants.GAME_ID));
			final String trump = body.getString(Constants.TRUMP);
			final String mode = body.getString(Constants.GAME_MODE);
			this.computeScore(this.gameService.getGames().get(gameID).getLatestTrick(), trump, mode,
			 this.gameService.getGames().get(gameID).getIsSuitFinished(), gameID).whenComplete((result, error) -> {
                if (error != null) {
                    LOGGER.error("Error when computing the score");
                    message.fail(417, "Error when computing the score");
                } else {
                    LOGGER.info("Computed score");
                    message.reply(result);
                }
            	});;
			});
		}


	/**
	 * perform a post request to business logic in order to compute the score, get
	 * the winning team and position
	 *
	 * @param trick the completed trick with which it computes the score
	 * @param trump used while computing the score
	 * @return a completable future of the json response
	 */
	public CompletableFuture<JsonObject> computeScore(final Trick trick, final String trump, final String mode,
			final List<Boolean> isSuitFinishedList, final UUID gameID) {
		this.gameService.getGames().get(gameID).incrementCurrentState();
		final int[] cards = trick.getCards().stream().mapToInt(Integer::parseInt).toArray();
		final boolean[] isSuitFinished = Booleans.toArray(isSuitFinishedList); 
		final JsonObject requestBody = new JsonObject()
				.put("trick", cards)
				.put("trump", Integer.parseInt(trump))
				.put("mode", mode)
				.put("isSuitFinished", isSuitFinished);
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		LOGGER.info("Computing the score");
		WebClient.create(this.vertx).post(PORT, LOCALHOST, "/games/computeScore")
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject())
				.sendJsonObject(requestBody, handler -> {
					if (handler.succeeded()) {
						this.gameService.getGames().get(gameID)
							.setTurn(handler.result().body().getInteger("winningPosition"));
						this.gameService.getGames().get(gameID).setScore(handler.result().body().getInteger("score"),
							handler.result().body().getBoolean("firstTeam"));
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
