package org.example;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameApiTest {
    private Vertx vertx = Vertx.vertx();
    private String usernameTest = "user1";
    private int numberOfPlayersTest = 4;

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

    /** The same user can't be added twice
     * */
    @Test
    void joinWithSameUser() {
        MainVerticle main = new MainVerticle(this.vertx);
        int gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        assertFalse(main.getGames().get(gameId).addUser(this.usernameTest));
    }

    @Test
    void playCard() {
    }
}