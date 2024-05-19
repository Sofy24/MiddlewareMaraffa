package game.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import game.*;
import repository.AbstractStatisticManager;
import game.utils.Constants;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class GameService {
    private final Map<UUID, GameVerticle> games = new ConcurrentHashMap<>();
    private final Vertx vertx;


    private AbstractStatisticManager statisticManager;

    public GameService(Vertx vertx) {
        this.vertx = vertx;

    }

    public GameService(Vertx vertx, AbstractStatisticManager statisticManager) {
        this.vertx = vertx;
        this.statisticManager = statisticManager;
    }

    public JsonObject createGame(Integer numberOfPlayers, String username, int expectedScore, String gameMode) {
        JsonObject jsonGame = new JsonObject();
        UUID newId = UUID.randomUUID();
        GameVerticle currentGame;
        try {
            if (this.statisticManager != null)
                currentGame = new GameVerticle(newId, username, numberOfPlayers, expectedScore, GameMode.valueOf(gameMode),
                        this.statisticManager);
            else
                currentGame = new GameVerticle(newId, username, numberOfPlayers, expectedScore, GameMode.valueOf(gameMode.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return jsonGame.put(Constants.INVALID, gameMode);
        }
        this.games.put(newId, currentGame);
        vertx.deployVerticle(currentGame);
        jsonGame.put(Constants.GAME_ID, String.valueOf(newId));
        return jsonGame;
    }

    public JsonObject joinGame(UUID gameID, String username) {
        JsonObject jsonJoin = new JsonObject();
        if (this.games.get(gameID) != null) {
            if (this.games.get(gameID).getNumberOfPlayersIn() < this.games.get(gameID).getMaxNumberOfPlayers()) {
                if (this.games.get(gameID).addUser(username)) {
                    jsonJoin.put(Constants.JOIN_ATTR, true);
                    return jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " joined by " + username);
                } else {
                    jsonJoin.put(Constants.ALREADY_JOINED, true);
                    return jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " already joined by " + username);
                }
            }
            jsonJoin.put(Constants.FULL, true);
            return jsonJoin.put(Constants.MESSAGE, "Reached the limit of maximum players in the game " + gameID);
        }
        jsonJoin.put(Constants.NOT_FOUND, false);
        jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " not found ");
        return jsonJoin;
    }

    public JsonObject startGame(UUID gameID) {
        JsonObject jsonStartGame = new JsonObject();
        if (this.games.get(gameID) != null) {
            if (this.games.get(gameID).startGame()) {
                jsonStartGame.put(Constants.START_ATTR, true);
                return jsonStartGame.put(Constants.MESSAGE, "The game " + gameID + " can start");
            } else {
                jsonStartGame.put(Constants.START_ATTR, false);
                return jsonStartGame.put(Constants.MESSAGE, "Not all the players are in");
            }
        }
        jsonStartGame.put(Constants.NOT_FOUND, false);
        return jsonStartGame.put(Constants.MESSAGE, "Game " + gameID + " not found");
    }

    public JsonObject canStart(UUID gameID) {
        JsonObject jsonCanStart = new JsonObject();
        if (this.games.get(gameID) != null) {
            if (this.games.get(gameID).canStart()) {
                jsonCanStart.put(Constants.START_ATTR, true);
                return jsonCanStart.put(Constants.MESSAGE, "The game " + gameID + " can start");
            } else {
                jsonCanStart.put(Constants.START_ATTR, false);
                return jsonCanStart.put(Constants.MESSAGE, "The game " + gameID + " can't start");
            }
        }
        jsonCanStart.put(Constants.NOT_FOUND, false);
        return jsonCanStart.put(Constants.MESSAGE, "Game " + gameID + " not found");
    }

    public JsonObject playCard(UUID gameID, String username, Card<CardValue, CardSuit> card) {
        JsonObject jsonPlayCard = new JsonObject();
        if (this.games.get(gameID) != null && this.games.get(gameID).canStart()) {
            return jsonPlayCard.put(Constants.PLAY, this.games.get(gameID).addCard(card, username));
        }
        jsonPlayCard.put(Constants.NOT_FOUND, false);
        return jsonPlayCard.put(Constants.PLAY, false);
    }

    public JsonObject chooseTrump(UUID gameID, String cardSuit, String username) {
        JsonObject jsonTrump = new JsonObject();
        if (this.games.get(gameID) != null) {
            if (this.games.get(gameID).getPositionByUsername(username) == this.games.get(gameID).getTurn()){
                CardSuit trump;
                try {
                    trump = CardSuit.valueOf(cardSuit);
                } catch (IllegalArgumentException e) {
                    trump = CardSuit.NONE;
                }
                this.games.get(gameID).chooseTrump(trump);
                jsonTrump.put(Constants.MESSAGE, trump + " setted as trump");
                if (trump.equals(CardSuit.NONE)) {
                    jsonTrump.put(Constants.TRUMP, false);
                    jsonTrump.put(Constants.ILLEGAL_TRUMP, true);
                    return jsonTrump;
                }
                jsonTrump.put(Constants.TRUMP, true);
                return jsonTrump;
            } else {
                jsonTrump.put(Constants.TRUMP, false);
                jsonTrump.put(Constants.NOT_ALLOWED, true);
                return jsonTrump.put(Constants.MESSAGE, "The user " + username + " is not allowed to choose the trump");
            }
        } else {
            jsonTrump.put(Constants.TRUMP, false);
            jsonTrump.put(Constants.NOT_FOUND, false);
            return jsonTrump.put(Constants.MESSAGE, "Game " + gameID + " not found");
        }
    }

    public boolean startNewRound(UUID gameID) {
        if (this.games.get(gameID) != null) {
            this.games.get(gameID).startNewRound();
            return true;
        }
        return false;
    }

    public JsonObject getState(UUID gameID) {
        JsonObject jsonState = new JsonObject();
        if (this.games.get(gameID) != null) {
            int lastState = this.games.get(gameID).getCurrentState().get();
            Trick currentTrick = this.games.get(gameID).getStates().get(lastState);
            if (currentTrick == null) {
                jsonState.put(Constants.NOT_FOUND, false);
                return jsonState.put(Constants.MESSAGE, "Trick not found");
            }
            jsonState.put(Constants.MESSAGE, currentTrick.toString());
            return jsonState;
        }
        jsonState.put(Constants.NOT_FOUND, false);
        return jsonState.put(Constants.MESSAGE, "Game " + gameID + " not found");
    }

    public JsonObject isRoundEnded(UUID gameID) {
        JsonObject jsonEnd = new JsonObject();
        if (this.games.get(gameID) != null) {
            Boolean isEnded = this.games.get(gameID).isRoundEnded();
            jsonEnd.put(Constants.ENDED, isEnded);
            jsonEnd.put(Constants.MESSAGE, isEnded);
            return jsonEnd;
        }
        jsonEnd.put(Constants.ENDED, false);
        return jsonEnd.put(Constants.MESSAGE, "Game " + gameID + " not found");
    }

    public JsonObject isGameEnded(UUID gameID) {
        JsonObject jsonEnd = new JsonObject();
        if (this.games.get(gameID) != null) {
            Boolean isEnded = this.games.get(gameID).isGameEnded();
            jsonEnd.put(Constants.ENDED, isEnded);
            //jsonEnd.put(Constants.MESSAGE, isEnded);
            return jsonEnd;
        }
        jsonEnd.put(Constants.ENDED, false);
        return jsonEnd.put(Constants.MESSAGE, "Game " + gameID + " not found");
    }

    public JsonObject makeCall(UUID gameID, String call, String username) {
        JsonObject jsonCall = new JsonObject();
        if (this.games.get(gameID) != null) {
            boolean success = this.games.get(gameID).makeCall(Call.fromUppercaseString(call.toUpperCase()), username);
            return jsonCall.put(Constants.MESSAGE, success);
        }
        jsonCall.put(Constants.NOT_FOUND, false);
        return jsonCall.put(Constants.MESSAGE, "Game " + gameID + " not found");
    }

    public JsonObject cardsOnHand(UUID gameID, String username) {
        JsonObject jsonCardsOnHand = new JsonObject();
        /*if(this.games.get(gameID) != null){
            Trick currentTrick = this.games.get(gameID).getStates().get(this.games.get(gameID).getCurrentState().get());
            jsonCardsOnHand.put(Constants.MESSAGE, currentTrick.toString());
            return jsonCardsOnHand;
        }*/
        jsonCardsOnHand.put(Constants.NOT_FOUND, false);
        return jsonCardsOnHand.put(Constants.MESSAGE, "Game " + gameID + " not found");
    }

    public JsonObject cardsOnTable(UUID gameID) {
        JsonObject jsonCardsOnTable = new JsonObject();
        /*if(this.games.get(gameID) != null){
            Trick currentTrick = this.games.get(gameID).getStates().get(this.games.get(gameID).getCurrentState().get());
            jsonCardsOnHand.put(Constants.MESSAGE, currentTrick.toString());
            return jsonCardsOnHand;
        }*/
        jsonCardsOnTable.put(Constants.NOT_FOUND, false);
        return jsonCardsOnTable.put(Constants.MESSAGE, "Game " + gameID + " not found");
    }

    public Map<UUID, GameVerticle> getGames() {
        return this.games;
    }

    // public JsonObject coins4(UUID gameID, String username) {
    //     JsonObject jsonCoins4 = new JsonObject();
    //     if (this.games.get(gameID) != null) {
    //         int turn = this.games.get(gameID).getPositionByUsername(username);
    //         if (turn != -1){
    //             this.games.get(gameID).setTurn(turn);
    //             return jsonCoins4.put(Constants.COINS_4_NAME, true);
    //         }
    //     }
    //     jsonCoins4.put(Constants.NOT_FOUND, false);
    //     jsonCoins4.put(Constants.COINS_4_NAME, false);
    //     return jsonCoins4.put(Constants.MESSAGE, "Game " + gameID + " not found");
    // }

    /**
     * @return the json with all the games and their properties
     */
    public JsonArray getJsonGames() {
        JsonArray jsonGames = new JsonArray();
        this.games.values().stream().map(GameVerticle::toJson).forEach(jsonGames::add);
        return jsonGames;
    }
}
