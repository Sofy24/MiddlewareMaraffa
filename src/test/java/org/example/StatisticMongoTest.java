package org.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    private final MongoStatisticManager mongoStatisticManager = new MongoStatisticManager(); //TODO andrebbe fatto contro il db {nome}_test

    private int gameId;

   /* public MainVerticle preapareMainVert(){
        MainVerticle main = new MainVerticle(this.vertx, mongoStatisticManager);
        this.gameId = main.createGame(this.usernameTest, numberOfPlayersTest);
        for (int i = 0; i < numberOfPlayersTest - 1; i++) {
            main.getGames().get(gameId).addUser(this.usernameTest + i);
        }
        return main;
    }
    @Test
    public void prepareGame(){
      MainVerticle main = preapareMainVert(); 
      var doc = this.mongoStatisticManager.getRecord(String.valueOf(this.gameId));
      System.out.println(doc.toString());
        assertNotNull(doc);
    }

    @Test
    public void makeTrick(){
        MainVerticle main = preapareMainVert();
        main.getGames().get(this.gameId).chooseSuit(cardTest.cardSuit());
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.THREE, CardSuit.CLUBS), this.usernameTest + "1"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.TWO, CardSuit.CLUBS), this.usernameTest + "2"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.ONE, CardSuit.CLUBS), this.usernameTest + "3"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.FOUR, CardSuit.CLUBS), this.usernameTest + "4"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.FIVE, CardSuit.CLUBS), this.usernameTest + "1"));
    }


    @Test
    public void make2Trick(){
        MainVerticle main = preapareMainVert();
        main.getGames().get(this.gameId).chooseSuit(cardTest.cardSuit());
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.THREE, CardSuit.CLUBS), this.usernameTest + "1"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.TWO, CardSuit.CLUBS), this.usernameTest + "2"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.ONE, CardSuit.CLUBS), this.usernameTest + "3"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.FOUR, CardSuit.CLUBS), this.usernameTest + "4"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.FIVE, CardSuit.CLUBS), this.usernameTest + "1"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.SIX, CardSuit.CLUBS), this.usernameTest + "2"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.HORSE, CardSuit.CLUBS), this.usernameTest + "4"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.KING, CardSuit.CLUBS), this.usernameTest + "3"));
        assertTrue(main.getGames().get(this.gameId).addCard(new Card<>(CardValue.KING, CardSuit.COINS), this.usernameTest + "1"));
    }*/
}
