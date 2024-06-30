package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import game.Team;
import game.service.User;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import userModule.UserService;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class UserTestIntegration {
	private final static UUID clientID = UUID.randomUUID();
	private Vertx vertx;
	private UserService userService;
	final static Dotenv dotenv = Dotenv.configure()
			.filename("env.example")
			.load();

	@BeforeAll
	public void setUp() {
		this.vertx = Vertx.vertx();
		this.userService = new UserService(this.vertx, Dotenv.load().get("USER_HOST", "localhost"),
				Integer.parseInt(Dotenv.load().get("USER_PORT", "3001")));
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
	public void testRegisterEvent(final VertxTestContext context) {
		this.userService.registerUser("user1", "password", "email@gmail.com").whenComplete((res, err) -> {
			context.verify(() -> {
				assertNull(res.getString("error"));
				assertEquals(res.getString("nickname"), "user1");
				context.completeNow();
			});
			// Otherwise timeout will be triggered to fail the test
		});
	}

	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void testRegisterFailFuture(final VertxTestContext context) {
		this.userService.registerUser("duplicateUser", "password", "email@gmail.com").whenComplete((res, err) -> {
			// Otherwise timeout will be triggered to fail the test
		}).join();
		this.userService.registerUser("duplicateUser", "password", "email@gmail.com").whenComplete((res, err) -> {
		}).whenComplete((res, err) -> {
			context.verify(() -> {
				assertNotNull(res.getString("error"));
				assertEquals(res.getString("error"), "User already exists");
				context.completeNow();
			});
		});
	}

	// context.verify(() -> {
	// });

	@Timeout(value = 10, unit = TimeUnit.MINUTES)
	@Test
	public void testLoginEvent(final VertxTestContext context) {
		this.userService.loginUser("user1", "password").whenComplete((res, err) -> {
			context.verify(() -> {
				assertNull(res.getString("error"));
				assertNotNull(res.getString("token"));
				context.completeNow();
			});
		});
	}

	@Timeout(value = 10, unit = TimeUnit.MINUTES)
	@Test
	public void testFetchUserInfo(final VertxTestContext context) {
		this.userService.getUserInfo("user1").whenComplete((res, err) -> {
			context.verify(() -> {
				assertNull(res.getString("error"));
				assertNotNull(res.getString("id"));
				context.completeNow();
			});
		});
	}

	@Timeout(value = 10, unit = TimeUnit.MINUTES)
	@Test
	public void testFetchUserInfoFailsOK(final VertxTestContext context) {
		this.userService.getUserInfo("asdrubale").whenComplete((res, err) -> {
			context.verify(() -> {
				assertNotNull(res.getString("error"));
				assertEquals(res.getString("error"), "Not Found");
				context.completeNow();
			});
		});
	}

	@Timeout(value = 10, unit = TimeUnit.MINUTES)
	@Test
	public void testLoginThrowsEvent(final VertxTestContext context) {
		this.userService.loginUser("user1", "pass").whenComplete((res, err) -> {
			context.verify(() -> {
				assertNotNull(res.getString("error"));
				assertEquals(res.getString("error"), "ko");
				context.completeNow();
			});
		});
		// try {
		// this.userService.loginUser("user1", "pass").whenComplete((res, err) -> {
		// System.out.println(res);
		// if (err == null)
		// context.completeNow();
		// // Otherwise timeout will be triggered to fail the test
		// }).join();
		// } catch (final RuntimeException e) {
		// context.completeNow();
		// }
	}

	@Timeout(value = 10, unit = TimeUnit.SECONDS)
	@Test
	public void testAfterGameHandler(final VertxTestContext context) {
		this.userService.registerUser("user2", "pwd", "mail").join();
		this.userService.registerUser("user3", "pwd", "mail").join();
		this.userService.registerUser("user4", "pwd", "mail").join();
		final Team team1 = new Team(List.of(new User("user1", clientID, false), new User("user2", clientID, false)),
				"teamA", 8);
		final Team team2 = new Team(List.of(new User("user3", clientID, false), new User("user4", clientID, false)),
				"teamB", 3);
		/** testing only the necessary part of the after round body */
		final JsonObject requestBody = new JsonObject().put("team1", team1).put("team2", team2);
		/**
		 * new JsonObject(requestBody.toString()) seems bad and a repetition but the
		 * json needs to be pushend onto the messagebus so the conversion to string is
		 * necessary and the method works with it
		 */
		this.userService.endGameHandler(new JsonObject(requestBody.toString())).whenComplete((res, err) -> {
			context.verify(() -> {
				assertTrue(res);
				context.completeNow();
			});
		});
	}
}
