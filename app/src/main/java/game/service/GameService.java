package game.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import game.Call;
import game.Card;
import game.CardSuit;
import game.CardValue;
import game.GameMode;
import game.GameVerticle;
import game.Trick;
import game.utils.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import repository.AbstractStatisticManager;



/**
 * TODO javadoc
 */
public class GameService {
	private final Map<UUID, GameVerticle> games = new ConcurrentHashMap<>();
	private final Vertx vertx;

	private AbstractStatisticManager statisticManager;

	public GameService(final Vertx vertx) {
		this.vertx = vertx;

	}

	public GameService(final Vertx vertx, final AbstractStatisticManager statisticManager) {
		this.vertx = vertx;
		this.statisticManager = statisticManager;
	}

	public JsonObject createGame(final Integer numberOfPlayers, final User user, final int expectedScore,
			final String gameMode) {
		final JsonObject jsonGame = new JsonObject();
		final UUID newId = UUID.randomUUID();
		GameVerticle currentGame;
		try {
			currentGame = new GameVerticle(newId, user, numberOfPlayers, expectedScore,
					GameMode.valueOf(gameMode.toUpperCase()),
					this.statisticManager);
			// TODO migliore gestione qui perche e' terribile ma per testare OK
		} catch (final IllegalArgumentException e) {
			return jsonGame.put(Constants.INVALID, gameMode);
		}
		this.games.put(newId, currentGame);
		this.vertx.deployVerticle(currentGame);
		currentGame.onCreateGame(user);
		jsonGame.put(Constants.GAME_ID, String.valueOf(newId));
		return jsonGame;
	}

	public JsonObject joinGame(final UUID gameID, final User user) {
		final JsonObject jsonJoin = new JsonObject();
		if (this.games.get(gameID) != null) {
			if (this.games.get(gameID).getNumberOfPlayersIn() < this.games.get(gameID).getMaxNumberOfPlayers()) {
				if (this.games.get(gameID).addUser(user)) {
					jsonJoin.put(Constants.JOIN_ATTR, true);
					return jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " joined by " + user.username());
				} else {
					jsonJoin.put(Constants.ALREADY_JOINED, true);
					return jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " already joined by " + user.username());
				}
			}
			jsonJoin.put(Constants.FULL, true);
			return jsonJoin.put(Constants.MESSAGE, "Reached the limit of maximum players in the game " + gameID);
		}
		jsonJoin.put(Constants.NOT_FOUND, false);
		jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " not found ");
		return jsonJoin;
	}

	/**
	 * @param gameID
	 * @return
	 */
	public JsonObject startGame(final UUID gameID) {
		final JsonObject jsonStartGame = new JsonObject();
		if (this.games.get(gameID) != null) {
			if (this.games.get(gameID).startGame()) {
				try{
					this.games.get(gameID).onStartGame();
					jsonStartGame.put(Constants.START_ATTR, true);
					jsonStartGame.put(Constants.MESSAGE, "The game " + gameID + " can start");
				} catch (final Exception e){
					jsonStartGame.put(Constants.START_ATTR, false);
					jsonStartGame.put(Constants.MESSAGE, "Error in starting the game");
				}
				return jsonStartGame;
			} else {
				jsonStartGame.put(Constants.START_ATTR, false);
				return jsonStartGame.put(Constants.MESSAGE, "Not all the players are in");
			}
		}
		jsonStartGame.put(Constants.NOT_FOUND, false);
		jsonStartGame.put(Constants.START_ATTR, false);
		return jsonStartGame.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject canStart(final UUID gameID) {
		final JsonObject jsonCanStart = new JsonObject();
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

    public JsonObject playCard(final UUID gameID, final String username, final Card<CardValue, CardSuit> card, final Boolean isSuitFinishedByPlayer) {
        final JsonObject jsonPlayCard = new JsonObject();
        if (this.games.get(gameID) != null && this.games.get(gameID).canStart()) {
			final GameVerticle game = this.games.get(gameID);
			game.setIsSuitFinished(isSuitFinishedByPlayer);
			final Boolean play = game.addCard(card, username);
			jsonPlayCard.put(Constants.PLAY, play);
				if (play && game.getLatestTrick().isCompleted()) {
					game.getGameSchema().addTrick(game.getCurrentTrick());
					if (this.statisticManager != null)
						this.statisticManager.updateRecordWithTrick(String.valueOf(gameID), game.getCurrentTrick());
					try {
						game.onTrickCompleted(game.getCurrentTrick());
					} catch (final Exception e) {
						jsonPlayCard.put(Constants.PLAY, false);
						jsonPlayCard.put(Constants.MESSAGE, "Failed to complete the trick");
						return jsonPlayCard;
					}
				}      
		} else {
			jsonPlayCard.put(Constants.NOT_FOUND, false);
			return jsonPlayCard.put(Constants.PLAY, false); 
		}  
		return jsonPlayCard;
	}

	public JsonObject chooseTrump(final UUID gameID, final String cardSuit, final String username) {
		final JsonObject jsonTrump = new JsonObject();
		if (this.games.get(gameID) != null) {
			if (this.games.get(gameID).getPositionByUsername(username) == this.games.get(gameID).getTurn()) {
				CardSuit trump;
				try {
					trump = CardSuit.valueOf(cardSuit);
				} catch (final IllegalArgumentException e) {
					trump = CardSuit.NONE;
				}
				this.games.get(gameID).chooseTrump(trump);
				jsonTrump.put(Constants.MESSAGE, trump + " setted as trump");
				if (CardSuit.NONE.equals(trump)) {
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

	public boolean startNewRound(final UUID gameID) {
		if (this.games.get(gameID) != null) {
			this.games.get(gameID).startNewRound();
			return true;
		}
		return false;
	}

	public JsonObject changeTeam(final UUID gameID, final String username, final String team, final Integer pos){
		final JsonObject jsonTeam = new JsonObject();
		if (this.games.get(gameID) != null) {
			jsonTeam.put(Constants.TEAM, this.games.get(gameID).changeTeam(username, team, pos));
			return jsonTeam;
		}
		jsonTeam.put(Constants.NOT_FOUND, false);
		return jsonTeam.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject getState(final UUID gameID) {
		final JsonObject jsonState = new JsonObject();
		if (this.games.get(gameID) != null) {
			final Trick currentTrick = this.games.get(gameID).getCurrentTrick();
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

	public JsonObject isRoundEnded(final UUID gameID) {
		final JsonObject jsonEnd = new JsonObject();
		if (this.games.get(gameID) != null) {
			final Boolean isEnded = this.games.get(gameID).isRoundEnded();
			if (isEnded)
				this.games.get(gameID).onEndRound();
			jsonEnd.put(Constants.ENDED, isEnded);
			jsonEnd.put(Constants.MESSAGE, isEnded);
			return jsonEnd;
		}
		jsonEnd.put(Constants.ENDED, false);
		return jsonEnd.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject isGameEnded(final UUID gameID) {
		final JsonObject jsonEnd = new JsonObject();
		if (this.games.get(gameID) != null) {
			final Boolean isEnded = this.games.get(gameID).isGameEnded();
			jsonEnd.put(Constants.ENDED, isEnded);
			return jsonEnd;
		}
		jsonEnd.put(Constants.ENDED, false);
		return jsonEnd.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject makeCall(final UUID gameID, final String call, final String username) {
		final JsonObject jsonCall = new JsonObject();
		if (this.games.get(gameID) != null) {
			final boolean success = this.games.get(gameID).makeCall(Call.fromUppercaseString(call.toUpperCase()),
					username);
			return jsonCall.put(Constants.MESSAGE, success);
		}
		jsonCall.put(Constants.NOT_FOUND, false);
		return jsonCall.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject cardsOnHand(final UUID gameID, final String username) {
		final JsonObject jsonCardsOnHand = new JsonObject();
		/*
		 * if(this.games.get(gameID) != null){ Trick currentTrick =
		 * this.games.get(gameID).getStates().get(this.games.get(gameID).getCurrentState
		 * ().get()); jsonCardsOnHand.put(Constants.MESSAGE, currentTrick.toString());
		 * return jsonCardsOnHand; }
		 */
		jsonCardsOnHand.put(Constants.NOT_FOUND, false);
		return jsonCardsOnHand.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject cardsOnTable(final UUID gameID) {
		final JsonObject jsonCardsOnTable = new JsonObject();
		/*
		 * if(this.games.get(gameID) != null){ Trick currentTrick =
		 * this.games.get(gameID).getStates().get(this.games.get(gameID).getCurrentState
		 * ().get()); jsonCardsOnHand.put(Constants.MESSAGE, currentTrick.toString());
		 * return jsonCardsOnHand; }
		 */
		jsonCardsOnTable.put(Constants.NOT_FOUND, false);
		return jsonCardsOnTable.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public Map<UUID, GameVerticle> getGames() {
		return this.games;
	}

	/**
	 * @return the json with all the games and their properties
	 */
	public JsonArray getJsonGames() {
		final JsonArray jsonGames = new JsonArray();
		this.games.values().stream().map(GameVerticle::toJson).forEach(jsonGames::add);
		return jsonGames;
	}
}
