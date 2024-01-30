package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.example.game.*;
import org.example.repository.AbstractStatisticManager;
import org.example.repository.MongoStatisticManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MainVerticle extends AbstractVerticle implements GameApi {
    private Map<Integer, GameVerticle> games = new ConcurrentHashMap<>();
    private final Vertx vertx;
    private int lastGameId;
    private AbstractStatisticManager statisticManager;

    //TODO doppio constructor per poter controllare l'invio di statistiche
    public MainVerticle(Vertx vertx) {
        this.lastGameId = 0; //find in MONGO the id game higher
        this.vertx = vertx;
    }/**TODO remove game when it's finished*/

    public MainVerticle(Vertx vertx, AbstractStatisticManager statisticManager) {
        this.lastGameId = 0; //find in MONGO the id game higher
        this.vertx = vertx;
        this.statisticManager = statisticManager;
    }/**TODO remove game when it's finished*/

    /**It starts the verticle*/
    @Override
    public void start() {
        /*vertx.eventBus().consumer("deploymentId", message -> {
            String methodToInvoke = message.body().toString();

            if ("getGames".equals(methodToInvoke)) {
                Object result = getGames();
                message.reply(result);
            }
        });*/
    }

    @Override
    public int createGame(String username, int numberOfPlayers) {//TODO insert id as param
        // lastGameId++;
        // int newId = lastGameId;
        int newId = new Random().nextInt(1000);
        GameVerticle currentGame = new GameVerticle(newId, username, numberOfPlayers, this.statisticManager);
        this.games.put(newId, currentGame);
        vertx.deployVerticle(currentGame);
        return newId;
    }

    @Override
    public boolean joinGame(String username, int idGame) {
        this.games.get(idGame).addUser(username);
        return false;
    }

    @Override
    public boolean playCard(int idGame, Card<CardValue, CardSuit> card, String username) {
        return this.games.get(idGame).addCard(card, username);
    }

    @Override
    public boolean CanStart(int idGame) {
        return this.games.get(idGame).canStart();
    }

    @Override
    public void chooseSuit(int idGame, CardSuit suit) {
        this.games.get(idGame).chooseSuit(suit);
    }

    @Override
    public void startNewRound(int idGame) {
        this.games.get(idGame).startNewRound();
    }


    public Map<Integer, GameVerticle> getGames() {
        return games;
    }

}
