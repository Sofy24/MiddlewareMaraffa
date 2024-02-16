package org.example;


import org.example.service.GameService;
import org.example.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(VertxUnitRunner.class)
public class GameTest {

    private static final String TEST_USER = "testUser";
    private static final int MARAFFA_PLAYERS = 4;
    private static final int UUID_SIZE = 36;
    private static final UUID FAKE_UUID = UUID.randomUUID();
    private Vertx vertx;
    private GameService gameService;
    /**
     * Before executing our test, let's deploy our verticle.
     * This method instantiates a new Vertx and deploy the verticle. Then, it waits
     * in the verticle has successfully
     * completed its start sequence (thanks to `context.asyncAssertSuccess`).
     *
     * @param context the test context.
     */
    @Before
    public void setUp(TestContext context) {
        this.vertx = Vertx.vertx();
        this.gameService = new GameService(this.vertx);
    }

    /** Create a new game (GameVerticle) and ensure that its UUID has been created correctly
     * */
    @Test
    public void createGameTest(TestContext context) {
        final Async async = context.async();
        JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
        assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length()); // Assuming UUID is 36 characters long
        async.complete();
    }

    /**If no one creates {@code FAKE_UUID} game, join response should have not found attribute*/
    @Test
    public void joinNotFoundGameTest(TestContext context) {
        final Async async = context.async();
        JsonObject gameResponse = this.gameService.joinGame(FAKE_UUID, TEST_USER);
        assertTrue(gameResponse.containsKey(Constants.NOT_FOUND));
        async.complete();
    }

    /**The join should add at maximum {@code MARAFFA_PLAYERS}*/
    @Test
    public void joinReachedLimitGameTest(TestContext context) {
        final Async async = context.async();
        JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
        assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
        for (int i = 0; i < MARAFFA_PLAYERS - 2; i++) {
            JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
            assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
        }
        JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + TEST_USER);
        assertTrue(joinResponse.containsKey(Constants.FULL));
        async.complete();
    }

    /** The same user can't be added twice* */
    @Test
    void joinWithSameUserTest(TestContext context) {
        final Async async = context.async();
        JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
        assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
        JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + TEST_USER);
        assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
        joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER);
        assertTrue(joinResponse.containsKey(Constants.ALREADY_JOINED));
        async.complete();
    }
}
