package org.example;

import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.example.service.GameService;
import org.example.utils.Constants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class GameTest {

    private static final String TEST_USER = "testUser";
    private static final String TRUMP = "coins";
    private static final String FAKE_TRUMP = "hammers";
    private static final CardSuit UNDEFINED_TRUMP = CardSuit.NONE;
    private static final int MARAFFA_PLAYERS = 4;
    private static final int UUID_SIZE = 36;
    private static final UUID FAKE_UUID = UUID.randomUUID();
    private static final Card<CardValue, CardSuit> TEST_CARD = new Card<>(CardValue.HORSE, CardSuit.CLUBS);
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
    @BeforeAll
    public void setUp() {
        this.vertx = Vertx.vertx();
        this.gameService = new GameService(this.vertx);
    }

    /**
     * This method, called after our test, just cleanup everything by closing the
     * vert.x instance
     *
     * @param context the test context
     */
    @AfterAll
    public void tearDown() {
        vertx.close();
    }

    /** Create a new game (GameVerticle) and ensure that its UUID has been created correctly
     * */
    @Test
    public void createGameTest(Vertx vertx, VertxTestContext context) {
        JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
        assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length()); // Assuming UUID is 36 characters long
        context.completeNow();
    }

    // /**If no one creates {@code FAKE_UUID} game, join response should have not found attribute*/
    // @Test
    // public void joinNotFoundGameTest(TestContext context) {
    //     final Async async = context.async();
    //     JsonObject gameResponse = this.gameService.joinGame(FAKE_UUID, TEST_USER);
    //     context.assertTrue(gameResponse.containsKey(Constants.NOT_FOUND));
    //     async.complete();
    // }

    // /**The join should add at maximum {@code MARAFFA_PLAYERS}*/
    // @Test
    // public void joinReachedLimitGameTest(TestContext context) {
    //     final Async async = context.async();
    //     JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
    //     assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
    //     for (int i = 0; i < MARAFFA_PLAYERS - 2; i++) {
    //         JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
    //         context.assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
    //     }
    //     JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + TEST_USER);
    //     context.assertTrue(joinResponse.containsKey(Constants.FULL));
    //     async.complete();
    // }

    // /** The same user can't be added twice* */
    // @Test
    // public void joinWithSameUserTest(TestContext context) {
    //     final Async async = context.async();
    //     JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
    //     context.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
    //     JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + TEST_USER);
    //     context.assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
    //     joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER);
    //     context.assertTrue(joinResponse.containsKey(Constants.ALREADY_JOINED));
    //     async.complete();
    // }

    // /**The round can't start if the trump is {@code CardSuit.NONE} and
    //  * if all players haven't joined it*/
    // @Test
    // public void chooseTrumpAndWaitAllPlayersTest(TestContext context){
    //     final Async async = context.async();
    //     JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
    //     context.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
    //     for (int i = 0; i < MARAFFA_PLAYERS - 2; i++) {
    //         JsonObject canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
    //         context.assertFalse(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
    //         JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
    //         context.assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
    //     }
    //     JsonObject canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
    //     context.assertFalse(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
    //     JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP);
    //     context.assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
    //     canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
    //     context.assertTrue(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
    //     async.complete();
    // }

    // /**The trump is not a legal suit*/
    // @Test
    // public void chooseWrongTest(TestContext context){
    //     final Async async = context.async();
    //     JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
    //     context.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
    //     for (int i = 0; i < MARAFFA_PLAYERS - 2; i++) {
    //         JsonObject canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
    //         context.assertFalse(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
    //         JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
    //         context.assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
    //     }
    //     JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), FAKE_TRUMP);
    //    context.assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
    //     context.assertTrue(chooseTrumpResponse.getBoolean(Constants.ILLEGAL_TRUMP));
    //     async.complete();
    // }

    // /**Reset the trump in order to start a new round */
    // @Test
    // public void startNewRoundTest(TestContext context){
    //     final Async async = context.async();
    //     JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
    //     context.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
    //     for (int i = 0; i < MARAFFA_PLAYERS - 2; i++) {
    //         JsonObject canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
    //         context.assertFalse(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
    //         JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
    //         context.assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
    //     }
    //     JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP);
    //     context.assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
    //     context.assertTrue(this.gameService.startNewRound(UUID.fromString(gameResponse.getString(Constants.GAME_ID))));
    //     context.assertEquals(UNDEFINED_TRUMP, this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getTrump());
    //     async.complete();
    // }

    // /** The card can be played only when the game is started*/
    // @Test
    // public void playCardTest(TestContext context) {
    //     final Async async = context.async();
    //     JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
    //     context.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
    //     for (int i = 0; i < MARAFFA_PLAYERS - 2; i++) {
    //         context.assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER, TEST_CARD));
    //         JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
    //         context.assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
    //     }
    //     context.assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER, TEST_CARD));
    //     JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP);
    //     context.assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
    //     context.assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER, TEST_CARD));
    //     async.complete();
    // }

}
