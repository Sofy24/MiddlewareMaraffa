package org.example;

import static org.junit.Assert.assertNotNull;

import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.example.game.GameMode;
import org.example.repository.AbstractStatisticManager;
import org.example.repository.MongoStatisticManager;
import org.example.service.GameService;
import org.example.utils.Constants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;


import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import java.util.UUID;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension.class)
public class StatisticMongoTest {
    private final String usernameTest = "user";
    private static final int MARAFFA_PLAYERS = 4;
    private static final int EXPECTED_SCORE = 11;
    private static final GameMode GAME_MODE = GameMode.CLASSIC;
    private Vertx vertx;
    private GameService gameService;
    private static final CardSuit UNDEFINED_TRUMP = CardSuit.NONE;
    private final Card<CardSuit, CardValue> cardTest = new Card<>(CardValue.THREE, CardSuit.CLUBS);
    private final MongoStatisticManager mongoStatisticManager = new MongoStatisticManager(); //TODO andrebbe fatto contro il db {nome}_test

    @BeforeAll
    public void setUp() {
        this.vertx = Vertx.vertx();
        this.gameService = new GameService(this.vertx, this.mongoStatisticManager);
    }

    /**
     * This method, called after our test, just cleanup everything by closing the
     * vert.x instance
     */
    @AfterAll
    public void tearDown() {
        vertx.close();
    }

    @Test
    public void prepareGame() {
        String gameID = this.gameService.createGame(MARAFFA_PLAYERS, this.usernameTest, EXPECTED_SCORE, GAME_MODE.toString())
                .getString(Constants.GAME_ID);
        var doc = this.mongoStatisticManager.getRecord(gameID);
        assertNotNull(doc);
    }
    
    @Test
    public void playCard(){
        String gameID = this.gameService.createGame(MARAFFA_PLAYERS, this.usernameTest, EXPECTED_SCORE, GAME_MODE.toString())
                .getString(Constants.GAME_ID);
            UUID gameId = UUID.fromString(gameID);
        for (int i = 2; i < MARAFFA_PLAYERS + 1 ; i++) {
            System.out.println(
                this.gameService.joinGame(gameId, this.usernameTest + i)
            );
        }
        
        this.gameService.chooseTrump(gameId, cardTest.cardSuit().toString());
        this.gameService.playCard(gameId, this.usernameTest, new Card<>(CardValue.ONE, CardSuit.CLUBS));
        this.gameService.playCard(gameId, this.usernameTest + "2", new Card<>(CardValue.TWO, CardSuit.CLUBS));
        this.gameService.playCard(gameId, this.usernameTest + "3", new Card<>(CardValue.THREE, CardSuit.CLUBS));
        this.gameService.playCard(gameId, this.usernameTest + "4", new Card<>(CardValue.FOUR, CardSuit.CLUBS));
        this.gameService.playCard(gameId, this.usernameTest + "3", new Card<>(CardValue.KING, CardSuit.CLUBS));
      
    }
   /*public MainVerticle preapareMainVert(){
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
