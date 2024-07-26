package repository;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import game.Card;
import game.CardSuit;
import game.CardValue;
import game.GameMode;
import game.service.GameService;
import game.service.User;
import game.utils.Constants;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;

/*
 * This class test che statistic are correctly saved in the db
 */
@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class StatisticMongoTest {
	private static final int TRICKS = 10;
	private final User userTest = new User("user", UUID.randomUUID(), false);
	private static final int MARAFFA_PLAYERS = 4;
	private static final int EXPECTED_SCORE = 11;
	private static final String PASSWORD = "1234";
	private static final GameMode GAME_MODE = GameMode.CLASSIC;
	private Vertx vertx;
	private GameService gameService;
	private static final CardSuit UNDEFINED_TRUMP = CardSuit.NONE;
	private final Card<CardSuit, CardValue> cardTest = new Card<>(CardValue.THREE, CardSuit.CLUBS);
	private final Boolean isSuitFinished = true;
	private final MongoStatisticManager mongoStatisticManager = new MongoStatisticManager(
			Dotenv.configure()
					.filename("env.example").load().get("MONGO_USER", "user"),
			Dotenv.configure()
					.filename("env.example").load().get("MONGO_PASSWORD", "password"),
			Dotenv.configure()
					.filename("env.example").load().get("MONGO_HOST", "localhost"),
			Integer.parseInt(Dotenv.configure()
					.filename("env.example").load().get("MONGO_PORT", "27127")),
			Dotenv.configure().filename("env.example").load().get("MONGO_DATABASE", "maraffa-test"));

	@BeforeAll
	public void setUp() {
		this.vertx = Vertx.vertx();
		this.gameService = new GameService(this.vertx, this.mongoStatisticManager);
	}

	/**
	 * This method, called after our test, just cleanup everything by closing the
	 * vert.x instance
	 */
	@AfterAll
	public void tearDown() {
		this.vertx.close();
	}

	@Test
	public void prepareGame() {
		final String gameID = this.gameService
				.createGame(MARAFFA_PLAYERS, this.userTest, EXPECTED_SCORE, GAME_MODE.toString())
				.getString(Constants.GAME_ID);
		final var doc = this.mongoStatisticManager.getRecord(gameID + "-0");
		assertNotNull(doc);
	}

	@Test
	public void playCard() {
		// TODO not finished
		final String gameID = this.gameService
				.createGame(MARAFFA_PLAYERS, this.userTest, EXPECTED_SCORE, GAME_MODE.toString())
				.getString(Constants.GAME_ID);
		final UUID gameId = UUID.fromString(gameID);
		for (int i = 2; i < MARAFFA_PLAYERS + 1; i++) {
			System.out.println(this.gameService.joinGame(gameId,
					new User(this.userTest.username() + i, this.userTest.clientID(), false), PASSWORD));
		}

		this.gameService.chooseTrump(gameId, this.cardTest.cardSuit().toString(), this.userTest.username());
		this.gameService.playCard(gameId, this.userTest.username(), new Card<>(CardValue.ONE, CardSuit.CLUBS),
				this.isSuitFinished);
		this.gameService.playCard(gameId, this.userTest.username() + "2", new Card<>(CardValue.TWO, CardSuit.CLUBS),
				this.isSuitFinished);
		this.gameService.playCard(gameId, this.userTest.username() + "3", new Card<>(CardValue.THREE, CardSuit.CLUBS),
				this.isSuitFinished);
		this.gameService.playCard(gameId, this.userTest.username() + "4", new Card<>(CardValue.FOUR, CardSuit.CLUBS),
				this.isSuitFinished);
		this.gameService.playCard(gameId, this.userTest.username() + "3", new Card<>(CardValue.KING, CardSuit.CLUBS),
				this.isSuitFinished);
	}


	@Test
	public void getGamesCompleted() {
		final var numGames = this.mongoStatisticManager.getGamesCompleted(TRICKS);
		assertNotNull(numGames);
		assertInstanceOf(Long.class, numGames);
	}
}
