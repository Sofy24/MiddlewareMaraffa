package org.example.service;



import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.example.game.GameVerticle;
import org.example.repository.AbstractStatisticManager;
import org.example.utils.Constants;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class GameService {
    private final Map<UUID, GameVerticle> games = new ConcurrentHashMap<>();
    private Vertx vertx;

    private AbstractStatisticManager statisticManager;

    public GameService(Vertx vertx) {
        this.vertx = vertx;
    }

    public GameService(Vertx vertx, AbstractStatisticManager statisticManager) {
        this.vertx = vertx;
        this.statisticManager = statisticManager;
    }

    public JsonObject createGame(Integer numberOfPlayers, String username) {
        JsonObject jsonGame = new JsonObject();
        UUID newId = UUID.randomUUID();
        GameVerticle currentGame = new GameVerticle(newId, username, numberOfPlayers);
        /*if (this.statisticManager != null ) currentGame = new GameVerticle(newId, username, numberOfPlayers, this.statisticManager);
        else currentGame = new GameVerticle(newId, username, numberOfPlayers);*/
        this.games.put(newId, currentGame);
        vertx.deployVerticle(currentGame);
        jsonGame.put(Constants.GAME_ID, String.valueOf(newId));
        return jsonGame;
    }

    public JsonObject joinGame(UUID gameID, String username) {
        JsonObject jsonJoin = new JsonObject();
        if(this.games.get(gameID) != null){
            if (this.games.get(gameID).getNumberOfPlayersIn() < this.games.get(gameID).getMaxNumberOfPlayers()) {
                if(this.games.get(gameID).addUser(username)) {
                    jsonJoin.put(Constants.JOIN_ATTR, true);
                    return jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " joined by " + username);
                } else {
                    jsonJoin.put(Constants.ALREADY_JOINED, true);
                    return jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " already joined by " + username);
                }
            }
            jsonJoin.put(Constants.FULL, true);
            return jsonJoin.put(Constants.MESSAGE, "Reached the limit of maximum players in the game "+ gameID);
        }
        jsonJoin.put(Constants.NOT_FOUND, false);
        return jsonJoin.put(Constants.MESSAGE, "Game "+ gameID + " not found ");
    }

    public JsonObject canStart(UUID gameID) {
        JsonObject jsonCanStart = new JsonObject();
        if(this.games.get(gameID) != null){
            if (this.games.get(gameID).canStart()) {
                return jsonCanStart.put(Constants.CAN_START_ATTR, "The game " + gameID + " can start");
            } else {
                return jsonCanStart.put(Constants.CAN_START_ATTR, "The game " + gameID + " can't start");
            }
        }
        jsonCanStart.put(Constants.NOT_FOUND, false);
        return jsonCanStart.put(Constants.CAN_START_ATTR, "Game "+ gameID +" not found");
    }

    public boolean playCard(UUID gameID, String username, Card<CardValue, CardSuit> card){
        if(this.games.get(gameID) != null){
            this.games.get(gameID).addCard(card, username);
            return true;
        }
        return false;
    }

    public boolean chooseTrump(UUID gameID, String cardSuit) {
        if(this.games.get(gameID) != null){
            this.games.get(gameID).chooseTrump(CardSuit.fromUppercaseString(cardSuit.toUpperCase()));
            return true;
        }
        return false;
    }

    public boolean startNewRound(UUID gameID) {
        if(this.games.get(gameID) != null){
            this.games.get(gameID).startNewRound();
            return true;
        }
        return false;
    }

    public Map<UUID, GameVerticle> getGames() {
        return games;
    }
}
