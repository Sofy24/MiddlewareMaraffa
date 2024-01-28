package org.example;

import io.vertx.core.Vertx;
import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GameApiTest {
    private final Vertx vertx = Vertx.vertx();
    private final String usernameTest = "user1";
    private final int numberOfPlayersTest = 4;

    private final CardSuit undefinedSuit = CardSuit.NONE;
    private final Card<CardValue, CardSuit> cardTest = new Card<>(CardValue.THREE, CardSuit.CLUBS);

    /** Create a new game (GameVerticle) and ensure that it has been added correctly
     * */
    @Test
    void createGame() {
        MainVerticle main = new MainVerticle(this.vertx);
        int numberOfGames = main.getGames().size();
        main.createGame(this.usernameTest, numberOfPlayersTest);
        int actualNumberOfGames = main.getGames().size();
        assertEquals(numberOfGames+1, actualNumberOfGames);
    }

    /** The join should add at maximum {@code numberOfPlayerTest}
     * */
    @Test
    void joinGame() {
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            assertTrue(main.getGames().get(gameId).addUser(this.usernameTest + i));
        }
        assertFalse(main.getGames().get(gameId).addUser(this.usernameTest+this.usernameTest));
    }

    /** The same user can't be added twice* */
    @Test
    void joinWithSameUser() {
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        assertFalse(main.getGames().get(gameId).addUser(this.usernameTest));
    }

    /** The card can be played only when the game is started*/
    @Test
    void playCard() {
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        assertFalse(main.getGames().get(gameId).addCard(this.cardTest, this.usernameTest));
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            main.getGames().get(gameId).addUser(this.usernameTest + i);
        }
        main.getGames().get(gameId).chooseSuit(cardTest.cardSuit());
        assertTrue(main.getGames().get(gameId).addCard(this.cardTest, this.usernameTest));
        assertEquals(Map.of(this.cardTest, this.usernameTest), main.getGames().get(gameId).getCurrentTrick().getCards());

    }

    /**The round can't start if the leading suit is {@code CardSuit.NONE} and
     * if all players have joined it*/
    @Test
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
    }

    /**Reset the leading suit to start a new round*/
    @Test
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
    }
}