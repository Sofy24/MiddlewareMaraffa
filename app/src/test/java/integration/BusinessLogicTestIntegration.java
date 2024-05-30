package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class BusinessLogicTestIntegration {

	private static final User TEST_USER = new User("testUser", UUID.randomUUID());
	private static final int MARAFFA_PLAYERS = 4;
	private GameService gameService;
	private Vertx vertx;
	private BusinessLogicController businessLogicController;

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

	/*Test if Maraffa is present */
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void MaraffaIsPresentTest(final VertxTestContext context) {
		int[] deck = new int[]{7,8,9,1,2,3,4,5,6,0}; 
		this.businessLogicController.getMaraffa(deck, 0).whenComplete((res, err) ->{
			context.verify(() -> {
				assertNull(res.getString("error"));
				assertEquals(res.getBoolean("maraffa"), true);
				context.completeNow();
			});
	});	
	}

	/*Test if Maraffa is not present */
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void MaraffaIsNotPresentTest(final VertxTestContext context) {
		int[] deck = new int[]{21,22,23,1,2,3,4,5,6,0}; 
		this.businessLogicController.getMaraffa(deck, 0).whenComplete((res, err) ->{
			context.verify(() -> {
				assertNull(res.getString("error"));
				assertEquals(res.getBoolean("maraffa"), false);
				context.completeNow();
			});
	});	
	}

}
