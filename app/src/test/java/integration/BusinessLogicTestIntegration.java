package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.gson.Gson;

import BLManagment.BusinessLogicController;
import game.Card;
import game.CardSuit;
import game.CardValue;
import game.GameMode;
import game.Trick;
import game.TrickImpl;
import game.service.GameService;
import game.service.User;
import game.utils.Constants;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class BusinessLogicTestIntegration {

	private static final User TEST_USER = new User("testUser", UUID.randomUUID());
	private static final int MARAFFA_PLAYERS = 4;
	private GameService gameService;
	private Vertx vertx;
	private BusinessLogicController businessLogicController;
	private static final List<Card<CardValue, CardSuit>> TEST_CARDS = List.of(new Card<>(CardValue.KING, CardSuit.CUPS),
			new Card<>(CardValue.KNAVE, CardSuit.COINS), new Card<>(CardValue.SEVEN, CardSuit.SWORDS),
			new Card<>(CardValue.HORSE, CardSuit.CLUBS));
	private static final List<Card<CardValue, CardSuit>> TEST_CARDS_OK = List.of(
			new Card<>(CardValue.KING, CardSuit.CUPS),
			new Card<>(CardValue.SEVEN, CardSuit.CUPS),
			new Card<>(CardValue.SIX, CardSuit.CUPS),
			new Card<>(CardValue.ONE, CardSuit.CLUBS));
	final static Dotenv dotenv = Dotenv.configure()
            .filename(".env.example")
            .load();

	@BeforeAll
	public void setUp() {
		this.vertx = Vertx.vertx();
		this.gameService = new GameService(this.vertx);
		this.businessLogicController = new BusinessLogicController(this.vertx, this.gameService, Integer.parseInt(dotenv.get("BUSINESS_LOGIC_PORT", "3000")), dotenv.get("BUSINESS_LOGIC_HOST"));

	}

	/**
	 * This method, called after our test, just cleanup everything by closing the
	 * vert.x instance
	 */
	@AfterAll
	public void tearDown() {
		this.vertx.close();
	}

	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void testgetShuffledDeckOK(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame((Integer) 4, TEST_USER, 41,
				GameMode.CLASSIC.toString());
		this.businessLogicController
				.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), 4)
				.whenComplete((res, err) -> {
					context.verify(() -> {
						assertNull(res.getString("error"));
						context.completeNow();
					});
				});
	}

	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void testCheckUserHandOK(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame((Integer) 4, TEST_USER, 41,
				GameMode.CLASSIC.toString());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
		}

		this.businessLogicController
				.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), 4)
				.whenComplete((res, err) -> {
					context.verify(() -> {
						assertNull(res.getString("error"));
						final var userHand = this.gameService.getGames()
								.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUserCards(TEST_USER.username());
						assertNotNull(userHand);
						context.completeNow();
					});
				});
	}

	@Timeout(value = 10, unit = TimeUnit.HOURS)
	@Test
	public void testUserPlayedCardIsRemoved(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame((Integer) 4, TEST_USER, 41,
				GameMode.CLASSIC.toString());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
		}

		this.businessLogicController
				.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), 4)
				.whenComplete((res, err) -> {
					context.verify(() -> {
						assertNull(res.getString("error"));
						final List<Card<CardValue, CardSuit>> userHand = this.gameService.getGames()
								.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUserCards(TEST_USER.username());
						assertNotNull(userHand);
						final Card<CardValue, CardSuit> fakePlayedCard = userHand.stream().toList()
								.get(MARAFFA_PLAYERS); // to not create another variable
						// TODO devi finirlo perche deve prendere l utente che puo giocare in quel
						// momento e non il primo a caso
						this.gameService.getGames()
								.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getTurn();
						this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
								TEST_USER.username(), fakePlayedCard, false);
						System.out.println(this.gameService.getGames()
								.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUserCards(TEST_USER.username()).size());
						context.completeNow();
					});
				});
	}

	// @Timeout(value = 10, unit = TimeUnit.SECONDS)
	// @Test
	// public void testJoin(final VertxTestContext context) {
	// this.createAGame().join();
	// for (int i = 0; i < 3; i++) {
	// this.joinTheGame("genericGameID", new User(TEST_USER.username() + i,
	// TEST_USER.clientID()))
	// .whenComplete((res, err) -> {
	// context.verify(() -> {
	// assertNull(res.getString("error"));
	// });
	// });
	// }
	// context.completeNow();
	// }
	/* Test if Maraffa is present */
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void MaraffaIsPresentTest(final VertxTestContext context) {
		final int[] deck = new int[] { 7, 8, 9, 1, 2, 3, 4, 5, 6, 0 };
		this.businessLogicController.getMaraffa(deck, 0).whenComplete((res, err) -> {
			context.verify(() -> {
				assertNull(res.getString("error"));
				assertEquals(res.getBoolean("maraffa"), true);
				context.completeNow();
			});
		});
	}

	/* Test if Maraffa is not present */
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void MaraffaIsNotPresentTest(final VertxTestContext context) {
		final int[] deck = new int[] { 21, 22, 23, 1, 2, 3, 4, 5, 6, 0 };
		this.businessLogicController.getMaraffa(deck, 0).whenComplete((res, err) -> {
			context.verify(() -> {
				assertNull(res.getString("error"));
				assertEquals(res.getBoolean("maraffa"), false);
				context.completeNow();
			});
		});
	}

	/* Test Team A committed a mistake */
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void TeamACommitMistake(final VertxTestContext context) {
		final CardSuit trump = CardSuit.COINS;
		final Trick trick = new TrickImpl(4, trump);
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, 41,
				GameMode.CLASSIC.toString());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
		}

		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			trick.addCard(TEST_CARDS.get(i), TEST_USER.username() + i);

		}
		final int[] cardArray = trick.getCards().stream().mapToInt(Integer::parseInt).toArray();
		System.out.println("the result i want" + Arrays.toString(cardArray));
		final JsonObject json = new JsonObject()
				.put(Constants.TRICK,
						Arrays.toString(cardArray));
		System.out.println("json" + json);
		final int[] cards = new Gson().fromJson(json.getString(Constants.TRICK), int[].class);
		System.out.println(Arrays.toString(cards));
		final List<Boolean> isSuitFinishedList = List.of(true, true, false, true);
		this.businessLogicController
				.computeScore(trick.getCards().stream().mapToInt(Integer::parseInt).toArray(), trick.getCardsAndUsers(),
						trump.value.toString(),
						GameMode.ELEVEN2ZERO.name(),
						isSuitFinishedList, UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.whenComplete((res, err) -> {
					context.verify(() -> {
						assertNull(res.getString("error"));
						assertEquals(res.getInteger("winningPosition"), -1);
						assertEquals(res.getBoolean("firstTeam"), true);
						// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).onEndRound();
						assertTrue(this.gameService.getGames()
						.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).isRoundEnded());

						context.completeNow();
					});
				});
	}

	/* Test Team B committed a mistake */
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void TeamBCommitMistake(final VertxTestContext context) {
		final CardSuit trump = CardSuit.COINS;
		final Trick trick = new TrickImpl(4, trump);
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, 41,
				GameMode.CLASSIC.toString());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
		}

		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			trick.addCard(TEST_CARDS.get(i), TEST_USER.username() + i);

		}
		final List<Boolean> isSuitFinishedList = List.of(true, true, true, false);
		this.businessLogicController
				.computeScore(trick.getCards().stream().mapToInt(Integer::parseInt).toArray(), trick.getCardsAndUsers(), trump.value.toString(),
						GameMode.ELEVEN2ZERO.name(),
						isSuitFinishedList, UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.whenComplete((res, err) -> {
					context.verify(() -> {
						assertNull(res.getString("error"));
						assertEquals(res.getInteger("winningPosition"), -1);
						assertEquals(res.getBoolean("firstTeam"), false);
						// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).onEndRound();
						assertTrue(this.gameService.getGames()
						.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).isRoundEnded());
						context.completeNow();
					});
				});
	}

	@Timeout(value = 10, unit = TimeUnit.HOURS)
	@Test
	public void computeScoreTest(final VertxTestContext context) {
		final CardSuit trump = CardSuit.CUPS;
		final Trick trick = new TrickImpl(4, trump);
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, 41,
				GameMode.CLASSIC.toString());
		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
		}
		this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + "2", "B", 0);
		this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + "3", "B", 1);
		final var dio = this.gameService.startGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		trick.addCard(TEST_CARDS_OK.get(0), TEST_USER.username());
		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			trick.addCard(TEST_CARDS_OK.get(i), TEST_USER.username() + i);
		}
		final List<Boolean> isSuitFinishedList = List.of(true, true, true, true);
		this.businessLogicController
				.computeScore(trick.getCards().stream().mapToInt(Integer::parseInt).toArray(), trick.getCardsAndUsers() ,trump.value.toString(),
						GameMode.ELEVEN2ZERO.name(),
						isSuitFinishedList, UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.whenComplete((res, err) -> {
					context.verify(() -> {
						assertNull(res.getString("error"));
						// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).onEndRound();
						assertTrue(this.gameService.getGames()
						.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).isRoundEnded());
						context.completeNow();
					});
				});
	}

}
