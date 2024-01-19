package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import org.example.game.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainVerticle extends AbstractVerticle implements GameApi {
    private Map<Integer, GameVerticle> games = new ConcurrentHashMap<>();
    private final Vertx vertx;
    private int lastGameId;

    public MainVerticle(Vertx vertx) {
        this.lastGameId = 0; //find in MONGO the id game higher
        this.vertx = vertx;
    }

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
    public int createGame(String username, int numberOfPlayers) {
        lastGameId++;
        int newId = lastGameId;
        GameVerticle currentGame = new GameVerticle(newId, username, numberOfPlayers);
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
    public boolean playCard(int idGame, Card<CardValue, CardSuit> card) {
        return this.games.get(idGame).addCard(card);
    }

    @Override
    public boolean CanStart(int idGame) {
        return this.games.get(idGame).canStart();
    }

    @Override
    public void chooseSuit(int idGame, CardSuit suit) {
        this.games.get(idGame).chooseSuit(suit);
    }

    public Map<Integer, GameVerticle> getGames() {
        return games;
    }

}
