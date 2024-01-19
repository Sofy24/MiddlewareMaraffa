package org.example;

import io.vertx.core.Vertx;
import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameApiTest {
    private final Vertx vertx = Vertx.vertx();
    private final String usernameTest = "user1";
    private final int numberOfPlayersTest = 4;

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
        assertFalse(main.getGames().get(gameId).addCard(this.cardTest));
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            main.getGames().get(gameId).addUser(this.usernameTest + i);
        }
        assertTrue(main.getGames().get(gameId).addCard(this.cardTest));
        assertEquals(List.of(this.cardTest), main.getGames().get(gameId).getCurrentTrick().getCards());

    }

    /** The game won't start if all players have joined it* */
    @Test
    void waitAllPlayers(){
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            assertFalse(main.getGames().get(gameId).canStart());
            main.getGames().get(gameId).addUser(this.usernameTest + i);
        }
        assertTrue(main.getGames().get(gameId).canStart());
    }
}