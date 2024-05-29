package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import BLManagment.BusinessLogicController;
import game.Team;
import game.service.GameService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import userModule.UserService;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class BusinessLogicTestIntegration {

	private Vertx vertx;
	private BusinessLogicController businessLogicController;

	@BeforeAll
	public void setUp() {
		this.vertx = Vertx.vertx();
		this.businessLogicController = new BusinessLogicController(this.vertx, new GameService(vertx));
	}

	/**
	 * This method, called after our test, just cleanup everything by closing the
	 * vert.x instance
	 */
	@AfterAll
	public void tearDown() {
		this.vertx.close();
	}

	/*Test if Maraffa is present */
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void MaraffaIsPresentTest(final VertxTestContext context) {
		JsonArray deck = new JsonArray(); //set the deck
		this.businessLogicController.getMaraffa(deck, 0, 0).whenComplete((res, err) ->{
			context.verify(() -> {
				assertNotNull(res.getString("error"));
				assertEquals(res.getString("maraffa"), true);
				context.completeNow();
			});
	});	
	}

	/*Test if Maraffa is not present */
	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void MaraffaIsNotPresentTest(final VertxTestContext context) {
		JsonArray deck = new JsonArray(); //set the deck
		this.businessLogicController.getMaraffa(deck, 0, 0).whenComplete((res, err) ->{
			context.verify(() -> {
				assertNotNull(res.getString("error"));
				assertEquals(res.getString("maraffa"), false);
				context.completeNow();
			});
	});	
	}

}
