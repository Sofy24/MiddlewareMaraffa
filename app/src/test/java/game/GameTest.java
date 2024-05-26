package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import BLManagment.BusinessLogicController;
import game.service.GameService;
import game.service.User;
import game.utils.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class GameTest {

	// private static final String TEST_USER = "testUser";
	private static final User TEST_USER = new User("testUser", UUID.randomUUID());
	private static final CardSuit TRUMP = CardSuit.COINS;
	private static final String FAKE_TRUMP = "hammers";
	private static final String CALL = "busso";
	private static final String FAKE_CALL = "suono";
	private static final String FAKE_GAME_MODE = "tresette";
	private static final CardSuit UNDEFINED_TRUMP = CardSuit.NONE;
	private static final GameMode GAME_MODE = GameMode.CLASSIC;
	private static final int MARAFFA_PLAYERS = 4;
	private static final int UUID_SIZE = 36;
	private static final int EXPECTED_SCORE = 11;
	private static final UUID FAKE_UUID = UUID.randomUUID();
	private static final Boolean IS_SUIT_FINISHED = true;
	private static final Card<CardValue, CardSuit> TEST_CARD = new Card<>(CardValue.HORSE, CardSuit.CLUBS);
	private static final List<Card<CardValue, CardSuit>> TEST_CARDS = List.of(new Card<>(CardValue.KING, CardSuit.CUPS),
			new Card<>(CardValue.KNAVE, CardSuit.COINS), new Card<>(CardValue.SEVEN, CardSuit.SWORDS), TEST_CARD);
	private Vertx vertx;
	private GameService gameService;
	private BusinessLogicController businessLogicController;

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
		this.businessLogicController = new BusinessLogicController(this.vertx, this.gameService);
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
	 * attribute
	 */
	@Test
	public void joinNotFoundGameTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.joinGame(FAKE_UUID, TEST_USER);
		assertTrue(gameResponse.containsKey(Constants.NOT_FOUND));
		context.completeNow();
	}

	/** The join should add at maximum {@code MARAFFA_PLAYERS} */
	@Test
	public void joinReachedLimitGameTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final JsonObject joinResponse = this.gameService.joinGame(
				UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				new User(TEST_USER.username() + TEST_USER.username(), TEST_USER.clientID()));
		assertTrue(joinResponse.containsKey(Constants.FULL));
		context.completeNow();
	}

	/** The same user can't be added twice* */
	@Test
	public void joinWithSameUserTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				new User(TEST_USER.username() + TEST_USER.username(), TEST_USER.clientID()));
		assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER);
		assertTrue(joinResponse.containsKey(Constants.ALREADY_JOINED));
		context.completeNow();
	}

	/** If all the players haven't joined, the game can't start */
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
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
			startGameResponse = this.gameService.startGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		}
		assertTrue(startGameResponse.getBoolean(Constants.START_ATTR));
		context.completeNow();
	}

	/** The round can't start if all players haven't joined it */
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
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);
			final JsonObject chooseTrumpResponse = this.gameService
					.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(), this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			final JsonObject canStartResponse = this.gameService
					.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
			assertTrue(canStartResponse.getBoolean(Constants.START_ATTR));
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}
		context.completeNow();
	}

	/** The trump is not a legal suit */
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
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
					.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), FAKE_TRUMP, this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertTrue(chooseTrumpResponse.getBoolean(Constants.ILLEGAL_TRUMP));
		
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}

		context.completeNow();
	}

	/** Reset the trump in order to start a new round */
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
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
					.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(), this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertTrue(this.gameService.startNewRound(UUID.fromString(gameResponse.getString(Constants.GAME_ID))));
			Assertions.assertEquals(UNDEFINED_TRUMP,
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getTrump());
		
		
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}
		context.completeNow();
	}

	/** The card can be played only when the game is started */
	@Test
	public void playCardTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertTrue(this.gameService
					.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(),
					  TEST_CARD, IS_SUIT_FINISHED)
					.getBoolean(Constants.PLAY));
		
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}


		context.completeNow();
	}


	/** Get a state */
	@Test
	public void getStateTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		JsonObject stateResponse = this.gameService
				.getState(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
		assertTrue(stateResponse.containsKey(Constants.NOT_FOUND));
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertTrue(this.gameService
					.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(),
					  TEST_CARD, IS_SUIT_FINISHED)
					.getBoolean(Constants.PLAY));
						
			for (int i = 1; i < MARAFFA_PLAYERS; i++) {
				assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
				 TEST_CARDS.get(i - 1), IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			}
			stateResponse = this.gameService.getState(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
			assertFalse(stateResponse.containsKey(Constants.NOT_FOUND));
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}	
		context.completeNow();
	}

	/**
	 * A round should not end if less than @code{{Constants.NUMBER_OF_CARDS}} are
	 * played
	 */
	@Test
	public void isRoundEndedTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS,
				new User(TEST_USER.username() + "0", TEST_USER.clientID()), EXPECTED_SCORE, GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertFalse(this.gameService.isRoundEnded(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getBoolean(Constants.ENDED));

			for (int i = 0; i < Constants.NUMBER_OF_CARDS; i++) {
			assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
			this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
			 TEST_CARDS.get(i % MARAFFA_PLAYERS), IS_SUIT_FINISHED)
					.getBoolean(Constants.PLAY));
			if (this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
					.getLatestTrick().isCompleted()) {
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.incrementCurrentState();
			}
		}
		assertTrue(this.gameService.isRoundEnded(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getBoolean(Constants.ENDED));

		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}	
		context.completeNow();
	}

	/** A game should end if a team reaches the expected score */
	@Test
	public void isGameEndedTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS,
				new User(TEST_USER.username() + "0", TEST_USER.clientID()), EXPECTED_SCORE, GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			final JsonObject startGameResponse = this.gameService
				.startGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
			assertTrue(startGameResponse.getBoolean(Constants.START_ATTR));
			assertFalse(this.gameService.isGameEnded(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getBoolean(Constants.ENDED));

			for (int i = 0; i < Constants.NUMBER_OF_CARDS; i++) {
			assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
			this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
			 TEST_CARDS.get(i % MARAFFA_PLAYERS), IS_SUIT_FINISHED)
					.getBoolean(Constants.PLAY));
			if (this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
					.getLatestTrick().isCompleted()) {
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.incrementCurrentState();
			}
		}
		//TODO finish it
		// assertTrue(this.gameService.isRoundEnded(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
		// 		.getBoolean(Constants.ENDED));

		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}	
		context.completeNow();
	}

	/** Only the first player can make a call */
	@Test
	public void onlyFirstPlayerCanMakeACallTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			
			JsonObject callResponse = this.gameService.makeCall(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				CALL, this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + 1) % MARAFFA_PLAYERS).username());
			assertFalse(callResponse.getBoolean(Constants.MESSAGE));
			callResponse = this.gameService.makeCall(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), CALL,
			this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(callResponse.getBoolean(Constants.MESSAGE));

		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}	
		context.completeNow();
	}

	/** The call is not a legal call */
	@Test
	public void chooseWrongCallTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			
			final JsonObject callResponse = this.gameService.makeCall(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
			FAKE_CALL, this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertFalse(callResponse.getBoolean(Constants.MESSAGE));
			

		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}	
		context.completeNow();
	}

	/** Returns all the games created or not found if there aren't games */
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

	/*
	 * A player can play only one card in their turn
	 */
	@Test
	public void playOnlyOneCardTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertTrue(this.gameService
					.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(),
					  TEST_CARD, IS_SUIT_FINISHED)
					.getBoolean(Constants.PLAY));
			assertFalse(this.gameService
					.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(),
					  TEST_CARDS.get(1), IS_SUIT_FINISHED)
					.getBoolean(Constants.PLAY));
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}
		context.completeNow();
	}

	/*
	 * Each player can play only in their turn
	 */
	@Test
	public void playOnlyInTheirTurnTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			final JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			final int turn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getTurn();

			assertEquals(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(), this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(turn).username());
			assertFalse(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + 1) % MARAFFA_PLAYERS).username(),
				  TEST_CARD, IS_SUIT_FINISHED)
				.getBoolean(Constants.PLAY));
			assertTrue(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(),
				  TEST_CARD, IS_SUIT_FINISHED)
				.getBoolean(Constants.PLAY));
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}
		context.completeNow();
	}


	/*
	 * An invalid user can't choose the trump
	 */
	@Test
	public void invalidUserCantChooseTrumpTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);

			JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + 1) % MARAFFA_PLAYERS).username());
			assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(),
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			final int turn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getTurn();

			assertEquals(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(), this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(turn).username());
			assertFalse(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + 1) % MARAFFA_PLAYERS).username(),
				  TEST_CARD, IS_SUIT_FINISHED)
				.getBoolean(Constants.PLAY));
			assertTrue(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
				 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(),
				  TEST_CARD, IS_SUIT_FINISHED)
				.getBoolean(Constants.PLAY));
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}

		context.completeNow();
	}


	/*
	 * A player can't play if the system doesn't know who has the 4 of coins
	 */
	@Test
	public void dontPlayWithout4Coins(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER, EXPECTED_SCORE,
				GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		assertFalse(this.gameService
				.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER.username(),
				 TEST_CARD, IS_SUIT_FINISHED)
				.getBoolean(Constants.PLAY));
		JsonObject chooseTrumpResponse = this.gameService
				.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(), TEST_USER.username());
		assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);
			chooseTrumpResponse = this.gameService
					.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(), TEST_USER.username());
			if (initialTurn != 0){
				assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			}

			chooseTrumpResponse = this.gameService
					.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(), this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertTrue(this.gameService
					.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					 this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(),
					  TEST_CARD, IS_SUIT_FINISHED)
					.getBoolean(Constants.PLAY));
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}
		context.completeNow();
	}

	/**
	 * The initial turn of the new round is correct when a new round starts
	 */
	@Test
	public void isTurnCorrectWhenRoundEndedTest(final VertxTestContext context) {
		final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS,
				new User(TEST_USER.username() + "0", TEST_USER.clientID()), EXPECTED_SCORE, GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					TEST_USER.username(), TEST_CARD, IS_SUIT_FINISHED).getBoolean(Constants.PLAY));
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}
		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);
			final JsonObject chooseTrumpResponse = this.gameService
					.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(), this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertFalse(this.gameService.isRoundEnded(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getBoolean(Constants.ENDED));

			for (int i = 0; i < Constants.NUMBER_OF_CARDS; i++) {
			assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
			this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
			 TEST_CARDS.get(i % MARAFFA_PLAYERS), IS_SUIT_FINISHED)
					.getBoolean(Constants.PLAY));
			if (this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
					.getLatestTrick().isCompleted()) {
				this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
						.incrementCurrentState();
			}
		}
		assertTrue(this.gameService.isRoundEnded(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getBoolean(Constants.ENDED));
		final int turn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
				.getTurn();
		assertEquals(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username(), this.gameService.getGames()
				.get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(turn).username());
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}
		context.completeNow();
	}

	/**
	 * The game mode is invalid, create returns "invalid" and getJsonGames "not
	 * found"
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

    //TODO increment current state could be done two times
    @Test
    public void computeScoreTest(final VertxTestContext context){
        final JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS,
				new User(TEST_USER.username() + "0", TEST_USER.clientID()), EXPECTED_SCORE, GAME_MODE.toString());
		Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
		for (int i = 1; i < MARAFFA_PLAYERS; i++) {
			final JsonObject joinResponse = this.gameService.joinGame(
					UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
					new User(TEST_USER.username() + i, TEST_USER.clientID()));
			assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
		}

		final CompletableFuture<JsonObject> future = this.businessLogicController.getShuffledDeck(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), MARAFFA_PLAYERS);
		try {
			final int firstPlayer = future.get().getInteger("firstPlayer");
			final int initialTurn = this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getInitialTurn();
			assertEquals(firstPlayer, initialTurn);
			final JsonObject chooseTrumpResponse = this.gameService
					.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP.name(), this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get(initialTurn).username());
			assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
			assertTrue(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).startGame());
			for (int i = 0; i < MARAFFA_PLAYERS; i++) {
                assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)),
                this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getUsers().get((initialTurn + i) % MARAFFA_PLAYERS).username(),
				 TEST_CARDS.get(i % MARAFFA_PLAYERS), IS_SUIT_FINISHED)
                        .getBoolean(Constants.PLAY));	
		    }
            final CompletableFuture<JsonObject> futureCompScore =  this.businessLogicController.computeScore(this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID)))
            .getLatestTrick(), TRUMP.value.toString(), GAME_MODE.name(), this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getIsSuitFinished() , UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
            final JsonObject compScoreJson = futureCompScore.get();
            assertEquals(3, compScoreJson.getInteger("score")); //TODO lo score è moltiplicato * 3, lo dobbiamo modificare nella BL
            assertEquals(1, compScoreJson.getInteger("winningPosition"));
            assertFalse(compScoreJson.getBoolean("firstTeam"));
		
        } catch (final InterruptedException e) {
			e.printStackTrace();
		} catch (final ExecutionException e) {
			e.printStackTrace();
		}
		context.completeNow();
    }
}
