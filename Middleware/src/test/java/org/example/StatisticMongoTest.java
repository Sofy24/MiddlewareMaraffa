package org.example;

import static org.junit.Assert.assertTrue;

import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.example.repository.AbstractStatisticManager;
import org.example.repository.MongoStatisticManager;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import io.vertx.core.Vertx;

public class StatisticMongoTest {
    private final Vertx vertx = Vertx.vertx();
    private final String usernameTest = "user1";
    private final int numberOfPlayersTest = 4;

    private final CardSuit undefinedSuit = CardSuit.NONE;
    private final Card<CardValue, CardSuit> cardTest = new Card<>(CardValue.THREE, CardSuit.CLUBS);
    private final AbstractStatisticManager mongoStatisticManager = new MongoStatisticManager(); //TODO andrebbe fatto contro il db {nome}_test

    private int gameId;

    @Test
    public void prepareGame(){
        MainVerticle main = new MainVerticle(this.vertx, mongoStatisticManager);
        this.gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            main.getGames().get(gameId).addUser(this.usernameTest + i);
        }
    }

    @Test
    public void testBefore(){
        assertTrue(true);
    }
}
