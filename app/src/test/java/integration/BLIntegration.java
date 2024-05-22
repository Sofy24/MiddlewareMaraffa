package integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import BLManagment.BusinessLogicController;
import game.GameMode;
import game.service.GameService;
import game.service.User;
import game.utils.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class BLIntegration {

	private static final User TEST_USER = new User("testUser", UUID.randomUUID());
	private static final int MARAFFA_PLAYERS = 4;
	private Vertx vertx;
	private GameService gameService;
	private BusinessLogicController businessLogicController;
	// private ChatService chatService;

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

	// private CompletableFuture<JsonObject> createAGame() {
	// return this.chatService.createGameChatHandler("genericGameID");
	// }

	// private CompletableFuture<JsonObject> joinTheGame(final String gameID, final
	// User user) {
	// return this.chatService.joinGameHandler(gameID, user);
	// }

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

	@Timeout(value = 1000, unit = TimeUnit.SECONDS)
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
								.getUserAndCards();
						assertNotNull(userHand);
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
}
