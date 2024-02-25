package org.example.service;



import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.example.game.*;
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
                jsonCanStart.put(Constants.CAN_START_ATTR, true);
                return jsonCanStart.put(Constants.MESSAGE, "The game " + gameID + " can start");
            } else {
                jsonCanStart.put(Constants.CAN_START_ATTR, false);
                return jsonCanStart.put(Constants.MESSAGE, "The game " + gameID + " can't start");
            }
        }
        jsonCanStart.put(Constants.NOT_FOUND, false);
        return jsonCanStart.put(Constants.MESSAGE, "Game "+ gameID +" not found");
    }

    public boolean playCard(UUID gameID, String username, Card<CardValue, CardSuit> card){
        if(this.games.get(gameID) != null && this.games.get(gameID).canStart()){
            this.games.get(gameID).addCard(card, username);
            return true;
        }
        return false;
    }

    public JsonObject chooseTrump(UUID gameID, String cardSuit) {
        JsonObject jsonTrump = new JsonObject();
        if(this.games.get(gameID) != null){
            CardSuit trump = CardSuit.valueOf(cardSuit);
            this.games.get(gameID).chooseTrump(trump);
            jsonTrump.put(Constants.MESSAGE, trump + " setted as trump");
            if (trump.equals(CardSuit.NONE)) {
                jsonTrump.put(Constants.TRUMP, false);
                jsonTrump.put(Constants.ILLEGAL_TRUMP, true);
                return jsonTrump;
            }
            jsonTrump.put(Constants.TRUMP, true);
            return jsonTrump;
        }
        jsonTrump.put(Constants.TRUMP, false);
        jsonTrump.put(Constants.NOT_FOUND, false);
        return jsonTrump.put(Constants.MESSAGE, "Game "+ gameID +" not found");
    }

    public boolean startNewRound(UUID gameID) {
        if(this.games.get(gameID) != null){
            this.games.get(gameID).startNewRound();
            return true;
        }
        return false;
    }

    public JsonObject getState(UUID gameID) {
        JsonObject jsonState = new JsonObject();
        if(this.games.get(gameID) != null){
            int lastState = this.games.get(gameID).getCurrentState().get();
            Trick currentTrick = this.games.get(gameID).getStates().get(lastState);
            if (currentTrick == null){
                jsonState.put(Constants.NOT_FOUND, false);
                return jsonState.put(Constants.MESSAGE, "Trick not found");
            }
            jsonState.put(Constants.MESSAGE, currentTrick.toString());
            return jsonState;
        }
        jsonState.put(Constants.NOT_FOUND, false);
        return jsonState.put(Constants.MESSAGE, "Game "+ gameID +" not found");
    }

    public JsonObject isRoundEnded(UUID gameID) {
        JsonObject jsonEnd = new JsonObject();
        if(this.games.get(gameID) != null){
            Boolean isEnded = this.games.get(gameID).isRoundEnded();
            jsonEnd.put(Constants.ENDED, isEnded);
            jsonEnd.put(Constants.MESSAGE, isEnded);
            return jsonEnd;
        }
        jsonEnd.put(Constants.ENDED, false);
        return jsonEnd.put(Constants.MESSAGE, "Game "+ gameID +" not found");
    }

    public JsonObject makeCall(UUID gameID, String call, String username) {
        JsonObject jsonCall = new JsonObject();
        if(this.games.get(gameID) != null){
            boolean success = this.games.get(gameID).makeCall(Call.fromUppercaseString(call.toUpperCase()), username);
            return jsonCall.put(Constants.MESSAGE, success);
        }
        jsonCall.put(Constants.NOT_FOUND, false);
        return jsonCall.put(Constants.MESSAGE, "Game "+ gameID +" not found");
    }

    public JsonObject cardsOnHand(UUID gameID, String username) {
        JsonObject jsonCardsOnHand = new JsonObject();
        /*if(this.games.get(gameID) != null){
            Trick currentTrick = this.games.get(gameID).getStates().get(this.games.get(gameID).getCurrentState().get());
            jsonCardsOnHand.put(Constants.MESSAGE, currentTrick.toString());
            return jsonCardsOnHand;
        }*/
        jsonCardsOnHand.put(Constants.NOT_FOUND, false);
        return jsonCardsOnHand.put(Constants.MESSAGE, "Game "+ gameID +" not found");
    }

    public JsonObject cardsOnTable(UUID gameID) {
        JsonObject jsonCardsOnTable = new JsonObject();
        /*if(this.games.get(gameID) != null){
            Trick currentTrick = this.games.get(gameID).getStates().get(this.games.get(gameID).getCurrentState().get());
            jsonCardsOnHand.put(Constants.MESSAGE, currentTrick.toString());
            return jsonCardsOnHand;
        }*/
        jsonCardsOnTable.put(Constants.NOT_FOUND, false);
        return jsonCardsOnTable.put(Constants.MESSAGE, "Game "+ gameID +" not found");
    }



    public Map<UUID, GameVerticle> getGames() {
        return games;
    }
}
