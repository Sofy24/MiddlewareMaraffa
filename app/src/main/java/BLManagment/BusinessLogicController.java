/**
 * The `BusinessLogicController` class in the BLManagment package handles game logic operations such as
 * starting rounds, completing tricks, checking for Maraffa, and validating card plays.
 */
package BLManagment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Booleans;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import game.Card;
import game.CardSuit;
import game.CardValue;
import game.service.GameService;
import game.utils.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class BusinessLogicController {
	private final Vertx vertx;
	private final int port; // = Integer.parseInt(Dotenv.load().get("BUSINESS_LOGIC_PORT", "3000"));
	private final String host;// = Dotenv.load().get("BUSINESS_LOGIC_HOST", "localhost");
	private static final Logger LOGGER = LoggerFactory.getLogger(BusinessLogicController.class);
	private final GameService gameService;

	public BusinessLogicController(final Vertx vertx, final GameService gameService, final int port,
			final String host) {
		this.vertx = vertx;
		this.gameService = gameService;
		this.port = port;
		this.host = host;
		this.startRound();
		this.trickCompleted();
		this.checkMaraffa();
	}

	/**
	 * @param numberOfPlayers
	 * @return
	 */
	public CompletableFuture<JsonObject> getShuffledDeck(final UUID gameID, final Integer numberOfPlayers) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		// final JsonObject startResponse = new JsonObject();
		WebClient.create(this.vertx).get(this.port, this.host, "/games/startRound")
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).send(handler -> {
					if (handler.succeeded()) {
						final JsonArray deck = handler.result().body().getJsonArray("deck");
						final Integer firstPlayer = handler.result().body().getInteger("firstPlayer");
						// startResponse.put("deck", deck);
						LOGGER.info("The deck is: " + deck);
						// startResponse.put("firstPlayer", firstPlayer);
						// startResponse.put(Constants.START_ATTR, true);
						if (this.gameService.getGames().get(gameID).getInitialTurn() == -1) {
							LOGGER.info("Round started");
							LOGGER.info("The first player is: " + firstPlayer);
							this.gameService.getGames().get(gameID).setInitialTurn(firstPlayer);
						}
						this.gameService.getGames().get(gameID)
								.handOutCards(deck.stream().map(el -> (Integer) el).toList());
						this.gameService.getGames().get(gameID).onNewRound();
						future.complete(handler.result().body());
					} else {
						LOGGER.error("Error in getting the shuffled deck " + handler.cause().getMessage());
						future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});

		return future;
	}

	/*
	 * start a new round
	 * 
	 * @param gameID the game ID for which to
	 * 
	 * @return a json object with the deck and the first player if there weren't any
	 * errors
	 */
	public void startRound() {
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
					// LOGGER.info("The first player is: " + firstPlayer);
					startResponse.put(Constants.START_ATTR, true);
					// this.gameService.getGames().get(gameID).setInitialTurn(firstPlayer);
				}
				if (error != null) {
					LOGGER.error("Error when starting the round");
					message.fail(417, "Error when starting the round");
				} else {
					LOGGER.info("Round started");
					message.reply(startResponse);
				}
			});
		});

	}

	/**
	 * perform all the actions the system needs to do when a trick is completed.
	 * There's also post request to
	 * business logic in order to compute the score, get the winning team and
	 * position
	 *
	 */
	public void trickCompleted() {
		this.vertx.eventBus().consumer("game-trickCommpleted:onTrickCommpleted", message -> {
			LOGGER.info("Received message: " + message.body());
			final JsonObject body = new JsonObject(message.body().toString());
			final UUID gameID = UUID.fromString(body.getString(Constants.GAME_ID));
			final String trump = body.getString(Constants.TRUMP);
			final String mode = body.getString(Constants.GAME_MODE);
			final int[] cards = new Gson().fromJson(body.getString(Constants.TRICK), int[].class);
			final Map<String, String> users = new Gson().fromJson(body.getString("userList"),
					new TypeToken<Map<String, String>>() {
					}.getType());
			System.out.println("int cards" + Arrays.toString(cards));
			this.computeScore(cards, users, trump, mode,
					this.gameService.getGames().get(gameID).getIsSuitFinished(), gameID)
					.whenComplete((result, error) -> {
						if (!result.containsKey("error")) {
							message.reply(result);
						}
						if (error != null) {
							LOGGER.error("Error when computing the score");
							message.fail(417, "Error when computing the score");
						} else {
							message.reply(result);
						}
					});
			;
		});
	}

	public CompletableFuture<JsonObject> computeScore(final int[] cards, final Map<String, String> users,
			final String trump,
			final String mode,
			final List<Boolean> isSuitFinishedList, final UUID gameID) {
		final boolean[] isSuitFinished = Booleans.toArray(isSuitFinishedList);
		final JsonObject requestBody = new JsonObject()
				.put("trick", cards)
				.put("trump", Integer.parseInt(trump))
				.put("mode", mode)
				.put("isSuitFinished", isSuitFinished);
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		LOGGER.info("Computing the score");
		System.out.println("MAP: " + users);
		WebClient.create(this.vertx).post(this.port, this.host, "/games/computeScore")
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject())
				.sendJsonObject(requestBody, handler -> {
					System.out.println(handler.result().body());
					if (handler.succeeded()) {
						final int winningPosition = handler.result().body().getInteger("winningPosition");
						final boolean firstTeam = handler.result().body().getBoolean("firstTeam");
						LOGGER.info(
								"GAME " + gameID.toString() + " score of last trick: " + cards.toString() + " -> "
										+ handler.result().body().getInteger("score"));
						if (winningPosition == -1) {
							LOGGER.info("ELeven zero because of mistake by team " + (firstTeam ? "1" : "2"));
							this.gameService.getGames().get(gameID).setScore(firstTeam);
							this.gameService.getGames().get(gameID).endRoundByMistake(firstTeam);

							this.gameService.getGames().get(gameID).clearIsSuitFinished();
							this.gameService.getGames().get(gameID).onEndRound();
							this.gameService.getGames().get(gameID).startNewRound();
							LOGGER.info("Game: " + gameID + " ended: cause 11 to zero");
							this.gameService.getGames().get(gameID).onStartGame();
						} else {
							this.gameService.getGames().get(gameID)
									.setTurnWithUser(users.get(String.valueOf(cards[winningPosition])));
							this.gameService.getGames().get(gameID).setScore(
									handler.result().body().getInteger("score"),
									firstTeam);
							LOGGER.info("Score computed");
						}

						future.complete(handler.result().body());
					} else {
						LOGGER.info("Error in computing the score");
						LOGGER.error("Error in computing the score");
						future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});
		return future;
	}

	/*
	 * check if Maraffa is present
	 * 
	 * @param
	 * 
	 * @return a json object with a boolean. True if Maraffa is present
	 */
	public void checkMaraffa() {
		this.vertx.eventBus().consumer("game-maraffa:onCheckMaraffa", message -> {
			LOGGER.info("Received message: " + message.body());
			final JsonObject body = new JsonObject(message.body().toString());
			final int suit = body.getInteger(Constants.SUIT);
			final String username = body.getString(Constants.USERNAME);
			final UUID gameID = UUID.fromString(body.getString(Constants.GAME_ID));
			final List<Card<CardValue, CardSuit>> userCardsTemp = this.gameService.getGames().get(gameID)
					.getUserCards(username);
			userCardsTemp.add(new Card<>(CardValue.ONE, CardSuit.fromValue(suit))); //TODo check if can be passed into the json
			final int[] userCards = userCardsTemp.stream().mapToInt(card -> card.getCardValue().intValue()).toArray();
			this.getMaraffa(userCards, suit).whenComplete((result, error) -> {
				if (error != null) {
					LOGGER.error("Error when checking Maraffa");
					message.fail(417, "Error when Error when checking Maraffa");
				} else {
					final Boolean maraffa = result.getBoolean("maraffa");
					LOGGER.info(maraffa ? "Maraffa is present" : "Maraffa is not present");
					message.reply(maraffa);
				}
			});
			;
		});

	}

	/**
	 * This Java method asynchronously sends a POST request with a JSON object to a
	 * specified endpoint and
	 * returns a CompletableFuture containing the response JsonObject or an error
	 * message.
	 * 
	 * @param deck
	 *            The `deck` parameter is an array of integers representing a deck
	 *            of cards. Each integer
	 *            value in the array corresponds to a specific card in the deck.
	 * @param suit
	 *            The `suit` parameter in the `getMaraffa` method represents the
	 *            suit of the cards in the
	 *            deck that you want to check for Maraffa. It is an integer value
	 *            that typically corresponds to a
	 *            specific suit in a standard deck of playing cards (e.g., Hearts,
	 *            Diamonds,
	 */
	public CompletableFuture<JsonObject> getMaraffa(final int[] deck, final int suit) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		final JsonObject requestBody = new JsonObject()
				.put("deck", deck)
				.put("suit", suit);
		WebClient.create(this.vertx).post(this.port, this.host, "/games/checkMaraffa")
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).sendJsonObject(requestBody, handler -> {
					if (handler.succeeded()) {
						future.complete(handler.result().body());
					} else {
						LOGGER.error("Error in getting Maraffa " + handler.cause().getMessage());
						future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});

		return future;
	}

	public void checkPlayCard() {
		this.vertx.eventBus().consumer("game-playCard:validate", message -> {
			LOGGER.info("Received message: " + message.body());
			final JsonObject body = new JsonObject(message.body().toString());
			final int card = body.getInteger("card");
			final String username = body.getString(Constants.USERNAME);
			final Boolean isCardTrump = body.getBoolean("isCardTrump");
			final UUID gameID = UUID.fromString(body.getString(Constants.GAME_ID));
			final int[] trick = this.gameService.getGames().get(gameID).getCurrentTrick().getCards().stream()
					.mapToInt(Integer::parseInt).toArray();
			final int[] userCards = this.gameService.getGames().get(gameID).getUserCards(username).stream()
					.mapToInt(userCard -> userCard.getCardValue().intValue()).toArray();
			this.validatePlayCard(trick, card, userCards, isCardTrump).whenComplete((result, error) -> {
				if (error != null) {
					LOGGER.error("Error when validating played card ");
					message.fail(417, "Error when Error when validating played card");
				} else {
					final Boolean isValid = result.getBoolean("valid");
					LOGGER.info(isValid ? "Card is valid" : "Card is not valid");
					message.reply(isValid);
				}
			});
		});

	}

	public CompletableFuture<JsonObject> validatePlayCard(final int[] trick, final int card, final int[] userCards,
			final boolean cardIsTrump) {
		final CompletableFuture<JsonObject> future = new CompletableFuture<>();
		final JsonObject requestBody = new JsonObject()
				.put("trick", trick)
				.put("card", card)
				.put("userCards", userCards)
				.put("cardIsTrump", cardIsTrump);
		WebClient.create(this.vertx).post(this.port, this.host, "/games/playCard-validation")
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject()).sendJsonObject(requestBody, handler -> {
					if (handler.succeeded()) {
						future.complete(handler.result().body());
					} else {
						LOGGER.error("Error in getting Maraffa " + handler.cause().getMessage());
						future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
					}
				});

		return future;
	}

}
