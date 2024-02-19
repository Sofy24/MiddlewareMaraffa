package org.example;

import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.example.service.GameService;
import org.example.utils.Constants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
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
    private static final List<Card<CardValue, CardSuit>> TEST_CARDS = List.of(new Card<>(CardValue.KING, CardSuit.CUPS), new Card<>(CardValue.KNAVE, CardSuit.COINS), new Card<>(CardValue.SEVEN, CardSuit.SWORDS));
//    private static final Card<CardValue, CardSuit> TEST_SECOND_CARD = new Card<>(CardValue.KING, CardSuit.CUPS);
//    private static final Card<CardValue, CardSuit> TEST_THIRD_CARD = new Card<>(CardValue.KNAVE, CardSuit.COINS);
//    private static final Card<CardValue, CardSuit> TEST_FORTH_CARD = new Card<>(CardValue.SEVEN, CardSuit.SWORDS);
    private Vertx vertx;
    private GameService gameService;

    /**
     * Before executing our test, let's deploy our verticle.
     * This method instantiates a new Vertx and deploy the verticle. Then, it waits
     * until the verticle has successfully
     * completed its start sequence (thanks to `context.asyncAssertSuccess`).
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
        vertx.close();
    }

    /** Create a new game (GameVerticle) and ensure that its UUID has been created correctly
     * */
    @Test
    public void createGameTest(VertxTestContext context) {
        JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
        Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length()); // Assuming UUID is 36 characters long
        context.completeNow();
    }

     /**If no one creates {@code FAKE_UUID} game, join response should have not found attribute*/
     @Test
     public void joinNotFoundGameTest(VertxTestContext context) {
         JsonObject gameResponse = this.gameService.joinGame(FAKE_UUID, TEST_USER);
         assertTrue(gameResponse.containsKey(Constants.NOT_FOUND));
         context.completeNow();
     }

     /**The join should add at maximum {@code MARAFFA_PLAYERS}*/
     @Test
     public void joinReachedLimitGameTest(VertxTestContext context) {
         JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
         Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
         for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
             JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
             assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
         }
         JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + TEST_USER);
         assertTrue(joinResponse.containsKey(Constants.FULL));
         context.completeNow();
     }

     /** The same user can't be added twice* */
     @Test
     public void joinWithSameUserTest(VertxTestContext context) {
         JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
         Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
         JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + TEST_USER);
         assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
         joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER);
         assertTrue(joinResponse.containsKey(Constants.ALREADY_JOINED));
         context.completeNow();
     }

     /**The round can't start if the trump is {@code CardSuit.NONE} and
      * if all players haven't joined it*/
     @Test
     public void chooseTrumpAndWaitAllPlayersTest(VertxTestContext context){
         JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
         Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
         for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
             JsonObject canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
             assertFalse(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
             JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
             assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
         }
         JsonObject canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
         assertFalse(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
         JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP);
         assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
         canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
         assertTrue(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
         context.completeNow();
     }

     /**The trump is not a legal suit*/
     @Test
     public void chooseWrongTest(VertxTestContext context){
         JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
         Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
         for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
             JsonObject canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
             assertFalse(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
             JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
             assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
         }
         JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), FAKE_TRUMP);
         assertFalse(chooseTrumpResponse.getBoolean(Constants.TRUMP));
         assertTrue(chooseTrumpResponse.getBoolean(Constants.ILLEGAL_TRUMP));
         context.completeNow();
     }

     /**Reset the trump in order to start a new round */
     @Test
     public void startNewRoundTest(VertxTestContext context){
         JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
         Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
         for (int i = 0; i < MARAFFA_PLAYERS - 2; i++) {
             JsonObject canStartResponse = this.gameService.canStart(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
             assertFalse(canStartResponse.getBoolean(Constants.CAN_START_ATTR));
             JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
             assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
         }
         JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP);
         assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
         assertTrue(this.gameService.startNewRound(UUID.fromString(gameResponse.getString(Constants.GAME_ID))));
         Assertions.assertEquals(UNDEFINED_TRUMP, this.gameService.getGames().get(UUID.fromString(gameResponse.getString(Constants.GAME_ID))).getTrump());
         context.completeNow();
     }

     /** The card can be played only when the game is started*/
     @Test
     public void playCardTest(VertxTestContext context) {
         JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
         Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
         for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
             assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER, TEST_CARD));
             JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
             assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
         }
         assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER, TEST_CARD));
         JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP);
         assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
         assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER, TEST_CARD));
         context.completeNow();
     }
     //TODO un giocatore non può giocare due volte in un turno


    /** Get a state*/
    @Test
    public void getStateTest(VertxTestContext context) {
        JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
        Assertions.assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length());
        for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
            assertFalse(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER, TEST_CARD));
            JsonObject joinResponse = this.gameService.joinGame(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i);
            assertTrue(joinResponse.containsKey(Constants.JOIN_ATTR));
        }
        JsonObject stateResponse = this.gameService.getState(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
        assertTrue(stateResponse.containsKey(Constants.NOT_FOUND));
        JsonObject chooseTrumpResponse = this.gameService.chooseTrump(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TRUMP);
        assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER, TEST_CARD));
        assertTrue(chooseTrumpResponse.getBoolean(Constants.TRUMP));
        for (int i = 0; i < MARAFFA_PLAYERS - 1; i++) {
            assertTrue(this.gameService.playCard(UUID.fromString(gameResponse.getString(Constants.GAME_ID)), TEST_USER + i, TEST_CARDS.get(i)));
        }
        stateResponse = this.gameService.getState(UUID.fromString(gameResponse.getString(Constants.GAME_ID)));
        assertFalse(stateResponse.containsKey(Constants.NOT_FOUND));
        context.completeNow();
    }

}
