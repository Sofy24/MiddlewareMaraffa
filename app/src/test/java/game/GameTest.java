package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import game.service.GameService;
import game.service.User;
import game.utils.Constants;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

/*
 * This class tests the game service using vertx
 */
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class GameTest {
	private static final User TEST_USER = new User("testUser", UUID.randomUUID(), false);
	private static final int FIRST_PLAYER = 0;
	private static final CardSuit TRUMP = CardSuit.COINS;
	private static final String PASSWORD = "1234";
	private static final String FAKE_TRUMP = "hammers";
	private static final String CALL = "busso";
	private static final String FAKE_CALL = "suono";
	private static final String FAKE_GAME_MODE = "tresette";
	private static final CardSuit UNDEFINED_TRUMP = CardSuit.NONE;
	private static final GameMode GAME_MODE = GameMode.CLASSIC;
	private static final int MARAFFA_PLAYERS = 4;
	private static final int UUID_SIZE = 36;
	private static final int EXPECTED_SCORE = 11;
	private static final int EXPECTED_POS = 1;
	private static final UUID FAKE_UUID = UUID.randomUUID();
	private static final Boolean IS_SUIT_FINISHED = true;
	private static final Card<CardValue, CardSuit> TEST_CARD = new Card<>(CardValue.HORSE, CardSuit.CLUBS);
	private static final List<Card<CardValue, CardSuit>> TEST_CARDS = List.of(new Card<>(CardValue.KING, CardSuit.CUPS),
			new Card<>(CardValue.KNAVE, CardSuit.COINS), new Card<>(CardValue.SEVEN, CardSuit.SWORDS), TEST_CARD);
	private Vertx vertx;
	private GameService gameService;
	final static Dotenv dotenv = Dotenv.configure()
			.filename("env.example")
			.load();

	/**
	 * Before executing our test, let's deploy our verticle. This method
	 * instantiates a new Vertx and deploy the verticle. Then, it waits until the
	 * verticle has successfully completed its start sequence (thanks to
	 * `context.asyncAssertSuccess`).
	 */
	@BeforeAll
	public void setUp() {
		this.vertx = Vertx.vertx();
		this.gameService = new GameService(this.vertx);
	}

	/**
	 * This method, called after our test, just cleanup everything by closing the
	 * vert.x instance
	 */
	@AfterAll
	public void tearDown() {
		this.vertx.close();
	}

	/**
	 * Create a new game (GameVerticle) and ensure that its UUID has been created
	 * correctly
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void createGameTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length()); // Assuming UUID is 36
		// characters long
		context.completeNow();
	}

	/**
	 * If no one creates {@code FAKE_UUID} game, join response should have not found
	 * 
	 * @param context
	 *            vertx test context
	 *            attribute
	 */
	@Test
	public void joinNotFoundGameTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.joinGame(FAKE_UUID, TEST_USER, PASSWORD);
		assertTrue(gameResponse.containsKey(Constants.NOT_FOUND));
		context.completeNow();
	}

	/**
	 * The join should add at maximum {@code MARAFFA_PLAYERS}
	 * 
	 * @param context
	 *            vertx test context
	 * 
	 */
	@Test
	public void joinReachedLimitGameTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final JsonObject joinResponse = this.gameService.joinGame(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				new User(TEST_USER.username() + TEST_USER.username(), TEST_USER.clientID(), false), PASSWORD);
		assertTrue(joinResponse.containsKey(Constants.FULL));
		context.completeNow();
	}

	/**
	 * The same user can't be added twice
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void joinWithSameUserTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				new User(TEST_USER.username() + TEST_USER.username(), TEST_USER.clientID(), false), PASSWORD);
		assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER,
				PASSWORD);
		assertTrue(joinResponse.containsKey(Constants.ALREADY_JOINED));
		context.completeNow();
	}

	/**
	 * If all the players haven't joined, the game can't start
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void theGameCantStartTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		JsonObject startGameResponse = this.gameService
				.startGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(startGameResponse.getBoolean(Constants.START_ATTR));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
			startGameResponse = this.gameService.startGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		}
		// JsonObject changeResponse = this.gameService.changeTeam(
		// UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
		// TEST_USER.username(), "B", 0);
		// assertTrue(changeResponse.getBoolean(Constants.TEAM));
		// changeResponse =
		// this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
		// TEST_USER.username() + 1, "B", EXPECTED_POS);
		startGameResponse = this.gameService.startGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		assertTrue(startGameResponse.getBoolean(Constants.START_ATTR));
		context.completeNow();
	}

	/**
	 * The round can't start if all players haven't joined it
	 * 
	 * @param context
	 *            vertx test context
	 * 
	 */
	@Test
	public void waitAllPlayersTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject canStartResponse = this.gameService
					.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
			assertFalse(canStartResponse.getBoolean(Constants.START_ATTR));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "B", 0);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 1, "B", EXPECTED_POS);
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		final JsonObject canStartResponse = this.gameService
				.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		assertTrue(canStartResponse.getBoolean(Constants.START_ATTR));
		context.completeNow();
	}

	/**
	 * The trump is not a legal suit
	 * 
	 * @param context
	 *            vertx test context
	 * 
	 */
	@Test
	public void chooseWrongTrumpTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject canStartResponse = this.gameService
					.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
			assertFalse(canStartResponse.getBoolean(Constants.START_ATTR));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), FAKE_TRUMP,
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		assertTrue(chooseTrumpResponse.getBoolean(Constants.ILLEGAL_TRUMP));
		context.completeNow();
	}

	/**
	 * Reset the trump in order to start a new round
	 * 
	 * @param context
	 *            vertx test context
	 * 
	 */
	@Test
	public void startNewRoundTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject canStartResponse = this.gameService
					.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
			assertFalse(canStartResponse.getBoolean(Constants.START_ATTR));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		assertTrue(this.gameService.startNewRound(UUID.fromString(gameResponse.getString(Constants.GAME_ID))));
		Assertions.assertEquals(UNDEFINED_TRUMP,
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.getTrump());
		context.completeNow();
	}

	/**
	 * The card can be played only when the game is started
	 * 
	 * @param context
	 * vertx test context
	 * 
	 */
	@Test
	public void playCardTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "B", 0);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 1, "B", EXPECTED_POS);
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		assertTrue(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username(),
						TEST_CARD, IS_SUIT_FINISHED, true)
				.getBoolean(Constants.PLAY));
		context.completeNow();
	}

	/**
	 * Get a state
	 * 
	 * @param context
	 *            vertx test context
	 * 
	 */
	@Test
	public void getStateTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject stateResponse = this.gameService
				.getState(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		assertTrue(stateResponse.containsKey(Constants.NOT_FOUND));
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "B", 0);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 1, "B", EXPECTED_POS);
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		assertTrue(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username(),
						TEST_CARD, IS_SUIT_FINISHED, true)
				.getBoolean(Constants.PLAY));

		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
							.getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
					TEST_CARDS.get(i - 1), IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
		}
		stateResponse = this.gameService.getState(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		assertFalse(stateResponse.containsKey(Constants.NOT_FOUND));
		context.completeNow();
	}

	/**
	 * A round should not end if less than @code{{Constants.NUMBER_OF_CARDS}} are
	 * played
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void isRoundEndedTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS,
				new User(TEST_USER.username() + "0", TEST_USER.clientID(), false), EXPECTED_SCORE, GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		assertTrue(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.startGame());
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(initialTurn);
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).onEndRound();
		assertFalse(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.isRoundEnded());
		//TODO 
		for (int i = 0; i < Constants.NUMBER_OF_CARDS; i++) {
			assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
							.getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
					TEST_CARDS.get(i % MARAFFA_PLAYERS), IS_SUIT_FINISHED, true)
					.getBoolean(Constants.PLAY));
		}
		// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).onEndRound();
		assertTrue(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.isRoundEnded());

		context.completeNow();
	}

	/** A game should end if a team reaches the expected score */
	// @Test
	// public void isGameEndedTest(final VertxTestContext context) {
	// final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS,
	// new User(TEST_USER.username() + "0", TEST_USER.clientID()), EXPECTED_SCORE,
	// GAME_MODE.toString());
	// Assertions.assertEquals(UUID_SIZE,
	// gameResponse.getString(Constants.GAME_ID).length());
	// for (int i = 1; i < MARAFFA_PLAYERS; i++) {
	// assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
	// TEST_USER.username(), TEST_CARD,
	// IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
	// final JsonObject joinResponse = this.gameService.joinGame(
	// UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
	// new User(TEST_USER.username() + i, TEST_USER.clientID()));
	// assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
	// }
	// JsonObject changeResponse = this.gameService.changeTeam(
	// UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
	// TEST_USER.username() + 0, "B", 0);
	// assertTrue(changeResponse.getBoolean(Constants.TEAM));
	// changeResponse =
	// this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
	// TEST_USER.username() + 1, "B", EXPECTED_POS);
	// assertTrue(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
	// .startGame());
	// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
	// .setInitialTurn(FIRST_PLAYER);
	// final int initialTurn = this.gameService.getGames()
	// .get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
	// final JsonObject chooseTrumpResponse = this.gameService
	// .chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
	// TRUMP.name(),
	// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
	// .getUsers().get(initialTurn).username());
	// assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
	// final JsonObject startGameResponse = this.gameService
	// .startGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
	// assertTrue(startGameResponse.getBoolean(Constants.START_ATTR));
	// assertFalse(this.gameService.isGameEnded(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
	// .getBoolean(Constants.ENDED));

	// for (int i = 0; i < Constants.NUMBER_OF_CARDS; i++) {
	// assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
	// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
	// .getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
	// TEST_CARDS.get(i % MARAFFA_PLAYERS), IS_SUIT_FINISHED)
	// .getBoolean(Constants.PLAY));
	// if
	// (this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
	// .getLatestTrick().isCompleted()) {
	// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
	// .incrementCurrentState();
	// }
	// }
	// // TODO finish it
	// //
	// assertTrue(this.gameService.isRoundEnded(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
	// // .getBoolean(Constants.ENDED));

	// // } catch (final InterruptedException e) {
	// // e.printStackTrace();
	// // } catch (final ExecutionException e) {
	// // e.printStackTrace();
	// // }
	// context.completeNow();
	// }

	/**
	 * Only the first player can make a call
	 * 
	 * @param context
	 *            vertx test context
	 * 
	 */
	@Test
	public void onlyFirstPlayerCanMakeACallTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		// final CompletableFuture<JsonObject> future = this.businessLogicController
		// .getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
		// MARAFFA_PLAYERS);
		// try {
		// final int firstPlayer = future.get().getInteger("firstPlayer");
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		// assertEquals(firstPlayer, initialTurn);

		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));

		JsonObject callResponse = this.gameService.makeCall(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				CALL, this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.getUsers().get((initialTurn + 1) % MARAFFA_PLAYERS).username());
		assertFalse(callResponse.getBoolean(Constants.MESSAGE));
		callResponse = this.gameService.makeCall(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), CALL,
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.getUsers().get(initialTurn).username());
		assertTrue(callResponse.getBoolean(Constants.MESSAGE));

		// } catch (final InterruptedException e) {
		// this.e.printStackTrace();
		// } catch (final ExecutionException e) {
		// this.e.printStackTrace();
		// }
		context.completeNow();
	}

	/**
	 * The call is not a legal call
	 * 
	 * @param context
	 *            vertx test context
	 * 
	 */
	@Test
	public void chooseWrongCallTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		// final CompletableFuture<JsonObject> future = this.businessLogicController
		// .getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
		// MARAFFA_PLAYERS);
		// try {
		// final int firstPlayer = future.get().getInteger("firstPlayer");
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		// assertEquals(firstPlayer, initialTurn);

		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));

		final JsonObject callResponse = this.gameService.makeCall(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				FAKE_CALL,
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.getUsers().get(initialTurn).username());
		assertFalse(callResponse.getBoolean(Constants.MESSAGE));

		// } catch (final InterruptedException e) {
		// e.printStackTrace();
		// } catch (final ExecutionException e) {
		// e.printStackTrace();
		// }
		context.completeNow();
	}

	/**
	 * Returns all the games created or not found if there aren't games
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void getGamesTest(final VertxTestContext context) {
		JsonArray gamesResponse = this.gameService.getJsonGames();
		// assertTrue(gamesResponse.isEmpty());
		this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE, GAME_MODE.toString());
		this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE, GAME_MODE.toString());
		this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE, GAME_MODE.toString());
		gamesResponse = this.gameService.getJsonGames();
		Assertions.assertFalse(gamesResponse.isEmpty());
		context.completeNow();
	}

	/**
	 * A player can play only one card in their turn
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void playOnlyOneCardTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 0, "B", 0);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 1, "B", EXPECTED_POS);
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		assertTrue(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username(),
						TEST_CARD, IS_SUIT_FINISHED, true)
				.getBoolean(Constants.PLAY));
		assertFalse(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username(),
						TEST_CARDS.get(1), IS_SUIT_FINISHED, true)
				.getBoolean(Constants.PLAY));
		context.completeNow();
	}

	/**
	 * Each player can play only in their turn
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void playOnlyInTheirTurnTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "B", 0);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 1, "B", EXPECTED_POS);
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		final int turn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getTurn();

		assertEquals(
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.getUsers().get(initialTurn).username(),
				this.gameService.getGames()
						.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(turn)
						.username());
		assertFalse(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get((initialTurn + 1) % MARAFFA_PLAYERS).username(),
						TEST_CARD, IS_SUIT_FINISHED, true)
				.getBoolean(Constants.PLAY));
		assertTrue(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username(),
						TEST_CARD, IS_SUIT_FINISHED, true)
				.getBoolean(Constants.PLAY));
		context.completeNow();
	}

	/**
	 * An invalid user can't choose the trump
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void invalidUserCantChooseTrumpTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "B", 0);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 1, "B", EXPECTED_POS);
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get((initialTurn + 1) % MARAFFA_PLAYERS).username());
		assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		final int turn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getTurn();

		assertEquals(
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.getUsers().get(initialTurn).username(),
				this.gameService.getGames()
						.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(turn)
						.username());
		// assertFalse(this.gameService
		// .playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
		// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
		// .getUsers().get((initialTurn + 1) % MARAFFA_PLAYERS).username(),
		// TEST_CARD, IS_SUIT_FINISHED)
		// .getBoolean(Constants.PLAY));
		// assertTrue(this.gameService
		// .playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
		// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
		// .getUsers().get(initialTurn).username(),
		// TEST_CARD, IS_SUIT_FINISHED)
		// .getBoolean(Constants.PLAY));
		context.completeNow();
	}

	/**
	 * A player can't play if the system doesn't know who has the 4 of coins
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void dontPlayWithout4Coins(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			// assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
			// 		TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "B", 0);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 1, "B", EXPECTED_POS);
		assertFalse(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER.username(),
						TEST_CARD, IS_SUIT_FINISHED, true)
				.getBoolean(Constants.PLAY));
		JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						TEST_USER.username());
		assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						TEST_USER.username());
		if (initialTurn != 0) {
			assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		}

		chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		assertTrue(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username(),
						TEST_CARD, IS_SUIT_FINISHED, true)
				.getBoolean(Constants.PLAY));
		context.completeNow();
	}

	/**
	 * The initial turn of the new round is correct when a new round starts
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void isTurnCorrectWhenRoundEndedTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS,
				new User(TEST_USER.username() + "0", TEST_USER.clientID(), false), EXPECTED_SCORE, GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED, true).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 0, "B", 0);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username() + 1, "B", EXPECTED_POS);
		this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.setInitialTurn(FIRST_PLAYER);
		final int initialTurn = this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
		final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
						this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
								.getUsers().get(initialTurn).username());
		assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).onEndRound();
		assertFalse(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.isRoundEnded());

		for (int i = 0; i < Constants.NUMBER_OF_CARDS; i++) {
			assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
							.getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
					TEST_CARDS.get(i % MARAFFA_PLAYERS), IS_SUIT_FINISHED, true)
					.getBoolean(Constants.PLAY));
		}
		// this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).onEndRound();
		assertTrue(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.isRoundEnded());
		final int turn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getTurn();
		assertEquals(
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.getUsers().get(initialTurn).username(),
				this.gameService.getGames()
						.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(turn)
						.username());
		context.completeNow();
	}

	/**
	 * The game mode is invalid, create returns "invalid" and getJsonGames "not
	 * found"
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void getGamesInvalidGameModeTest(final VertxTestContext context) {
		final JsonObject createResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				FAKE_GAME_MODE);
		final JsonArray gamesResponse = this.gameService.getJsonGames();
		assertTrue(createResponse.containsKey(Constants.INVALID));
		// assertTrue(gamesResponse.isEmpty());
		context.completeNow();
	}

	/**
	 * A user is able to change the team
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void changeTeamTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
		}
		final JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "A", EXPECTED_POS);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		context.completeNow();
	}

	/**
	 * A user can't change the team if the game has already started
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void changeNotAllowedWhilePlayingTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
		}
		this.gameService
				.startGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		final JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "A", EXPECTED_POS - 1);
		assertFalse(changeResponse.getBoolean(Constants.TEAM));
		context.completeNow();
	}

	/**
	 * A user can't change the team if the position specified is invalid
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void wrongPositionTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		assertThrows(IndexOutOfBoundsException.class, () -> {
			this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), "A", EXPECTED_POS);
		});
		context.completeNow();
	}

	/**
	 * Users can change their position in the same team they are
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void changePositionSameTeamTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID(), false), PASSWORD);
		}
		JsonObject changeResponse = this.gameService.changeTeam(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "A", EXPECTED_POS);
		changeResponse = this.gameService.changeTeam(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				TEST_USER.username(), "A", EXPECTED_POS - 1);
		assertTrue(changeResponse.getBoolean(Constants.TEAM));
		context.completeNow();
	}

	/**
	 * Creator can set a password for the game
	 */
	@Test
	public void setPasswordTest(final VertxTestContext context) {
		assertFalse(this.gameService.setPassword(FAKE_UUID,
				PASSWORD));
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		assertTrue(this.gameService.setPassword(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				PASSWORD));
		context.completeNow();
	}

	/**
	 * Start another game with the same users, same game mode, same expected score
	 * of the previos one
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void newGameTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
		GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		final JsonObject newGameResponse = this.gameService
				.newGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		this.gameService.getGames().get(UUID.fromString(newGameResponse.getString("newGameID")));
		assertTrue(newGameResponse.getBoolean(Constants.NEW_GAME_CREATION));
		context.completeNow();
	}

	/**
	 * Check if the user is removed from the game
	 * 
	 * @param context
	 *            vertx test context
	 */
	@Test
	public void removeUserTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
		GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + 0, TEST_USER.clientID(), false), PASSWORD);
		final JsonObject removeResponse = this.gameService.removeUser(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER.username() + 0);
		assertFalse(removeResponse.containsKey(Constants.NOT_FOUND));
		context.completeNow();
	}
}
