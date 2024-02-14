package org.example;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.example.service.GameService;
import org.example.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.WebClient;

@RunWith(VertxUnitRunner.class)
public class GameTest {

    private static final String TEST_USER = "testUser";
    private static final int MARAFFA_PLAYERS = 4;
    private Vertx vertx;
    private GameService gameService;
    /**
     * Before executing our test, let's deploy our verticle.
     * <p/>
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



    @Test
    public void testCreateGame(TestContext context) {
        final Async async = context.async();
        JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
        assertEquals(36, gameResponse.getString(Constants.GAME_ID).length()); // Assuming UUID is 36 characters long
        async.complete();
    }
}
