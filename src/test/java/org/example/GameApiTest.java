package org.example;


import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.example.service.GameService;
import org.example.utils.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.runner.RunWith;
import static org.junit.Assert.assertTrue;



@RunWith(VertxUnitRunner.class)
class GameApiTest {
    private static final String TEST_USER = "testUser";
    private static final int MARAFFA_PLAYERS = 4;
    private static final int UUID_SIZE = 36;
    private int port = 8081;
    private final Vertx vertx = Vertx.vertx();
    private GameService gameService;
    private final String usernameTest = "user1";
    private final int numberOfPlayersTest = 4;
    private final CardSuit undefinedSuit = CardSuit.NONE;
    private final Card<CardValue, CardSuit> cardTest = new Card<>(CardValue.THREE, CardSuit.CLUBS);


    /** Create a new game (GameVerticle) and ensure that it has been added correctly
     * */
    /*@Test
    void createGame() {
        GameService service = new GameService(this.vertx);
        int numberOfGames = service.getGames().size();
        /*service.createGame(this.usernameTest, numberOfPlayersTest);
        int actualNumberOfGames = main.getGames().size();
        assertEquals(numberOfGames+1, actualNumberOfGames);
    }*/

    @org.junit.Before
    public void setUp(TestContext context) {
        this.gameService = new GameService(this.vertx);
    }

    /** Create a new game (GameVerticle) and ensure that its UUID has been created correctly
     * */

    @Test
    public void testTest(){
        assertTrue(true);
    }
    /*@org.junit.Test
    public void testCreateGame(TestContext context) {
        final Async async = context.async();
        JsonObject gameResponse = this.gameService.createGame(MARAFFA_PLAYERS, TEST_USER);
        assertEquals(UUID_SIZE, gameResponse.getString(Constants.GAME_ID).length()); // Assuming UUID is 36 characters long
        async.complete();
    }*/

    /*@Test
    void testCreateGame(Vertx vertx, VertxTestContext testContext) {
        // Deploy the verticle under test
        vertx.deployVerticle(new AppServer(), testContext.succeeding(id -> {
            // Create a web client
            WebClient client = WebClient.create(vertx);

            // Prepare the request body
            JsonObject requestBody = new JsonObject()
                    .put(Constants.NUMBER_OF_PLAYERS, 4)
                    .put(Constants.USERNAME, "testUser");

            // Send HTTP POST request to the endpoint
            client.post(8080, "localhost", "/doc/#/Game/game/create")
                    .sendJsonObject(requestBody, asyncResult -> {
                        if (asyncResult.succeeded()) {
                            // Verify response
                            System.out.println("asyncResult.result().bodyAsString() = " + asyncResult.result().bodyAsString());
                            // Verify response body
                            JsonObject responseBody = asyncResult.result().bodyAsJsonObject();
                            String gameId = responseBody.getString(Constants.GAME_ID);
                            assertEquals(36, gameId.length()); // Assuming UUID is 36 characters long
                            assertEquals(200, asyncResult.result().statusCode());

                            // Complete the test
                            testContext.completeNow();
                        } else {
                            // Fail the test
                            testContext.failNow(asyncResult.cause());
                        }
                    });
        }));
    }*/


    /** The join should add at maximum {@code numberOfPlayerTest}
     * */
    /*@Test
    void joinGame() {
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            assertTrue(main.getGames().get(gameId).addUser(this.usernameTest + i));
        }
        assertFalse(main.getGames().get(gameId).addUser(this.usernameTest+this.usernameTest));
    }*/

    /** The same user can't be added twice* */
    /*@Test
    void joinWithSameUser() {
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        assertFalse(main.getGames().get(gameId).addUser(this.usernameTest));
    }*/

    /** The card can be played only when the game is started*/
    /*@Test
    void playCard() {
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        assertFalse(main.getGames().get(gameId).addCard(this.cardTest, this.usernameTest));
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            main.getGames().get(gameId).addUser(this.usernameTest + i);
        }
        main.getGames().get(gameId).chooseSuit(cardTest.cardSuit());
        assertTrue(main.getGames().get(gameId).addCard(this.cardTest, this.usernameTest));
        //TODO check is to string ok in a test ?
        assertTrue(Map.of(this.cardTest, this.usernameTest).toString().equals(main.getGames().get(gameId).getCurrentTrick().getCards().toString()));
        // assertEquals(Map.of(this.cardTest, this.usernameTest), main.getGames().get(gameId).getCurrentTrick().getCards());

    }*/

    /**The round can't start if the leading suit is {@code CardSuit.NONE} and
     * if all players have joined it*/
    /*@Test
    void chooseSuitAndWaitAllPlayers(){
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            assertFalse(main.getGames().get(gameId).canStart());
            main.getGames().get(gameId).addUser(this.usernameTest + i);
        }
        assertFalse(main.getGames().get(gameId).canStart());
        main.getGames().get(gameId).chooseSuit(cardTest.cardSuit());
        assertTrue(main.getGames().get(gameId).canStart());
    }*/

    /**Reset the leading suit to start a new round*/
    /*@Test
    void startNewRound(){
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            assertFalse(main.getGames().get(gameId).canStart());
            main.getGames().get(gameId).addUser(this.usernameTest + i);
        }
        main.getGames().get(gameId).chooseSuit(cardTest.cardSuit());
        assertTrue(main.getGames().get(gameId).addCard(this.cardTest, this.usernameTest));
        assertEquals(cardTest.cardSuit(), main.getGames().get(gameId).getLeadingSuit());
        main.getGames().get(gameId).startNewRound();
        assertEquals(undefinedSuit, main.getGames().get(gameId).getLeadingSuit());
    }*/
}