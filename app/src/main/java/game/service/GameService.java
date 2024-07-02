package game.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import game.Call;
import game.Card;
import game.CardSuit;
import game.CardValue;
import game.GameMode;
import game.GameVerticle;
import game.Trick;
import game.TrickImpl;
import game.utils.Constants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import repository.AbstractStatisticManager;
import server.WebSocketVertx;

/**
 * TODO javadoc
 */
public class GameService {
	private final Map<UUID, GameVerticle> games = new ConcurrentHashMap<>();
	private final Vertx vertx;
	private WebSocketVertx webSocket;
	private final static Boolean DEBUG = false;

	private AbstractStatisticManager statisticManager;

	public GameService(final Vertx vertx) {
		this.vertx = vertx;

	}

	public GameService(final Vertx vertx, final AbstractStatisticManager statisticManager) {
		this.vertx = vertx;
		this.statisticManager = statisticManager;
	}

	public GameService(final Vertx vertx, final AbstractStatisticManager statisticManager,
			final WebSocketVertx webSocket) {
		this.vertx = vertx;
		this.statisticManager = statisticManager;
		this.webSocket = webSocket;
	}

	public JsonObject createGame(final Integer numberOfPlayers, final User user, final int expectedScore,
			final String gameMode) {
		final JsonObject jsonGame = new JsonObject();
		final UUID newId = UUID.randomUUID();
		GameVerticle currentGame;
		try {
			currentGame = new GameVerticle(newId, user, numberOfPlayers, expectedScore,
					GameMode.valueOf(gameMode.toUpperCase()),
					this.statisticManager, this.webSocket);
			// TODO migliore gestione qui perche e' terribile ma per testare OK
		} catch (final IllegalArgumentException e) {
			jsonGame.put(Constants.ERROR, "Invalida modalità di gioco " + gameMode);
			return jsonGame.put(Constants.INVALID, gameMode);
		}
		this.games.put(newId, currentGame);
		this.vertx.deployVerticle(currentGame);
		currentGame.onCreateGame(user);
		// TODO molto poco bello..... ma per ora funziona
		if (this.webSocket != null)
			this.webSocket
					.broadcastToEveryone(new JsonObject()
							.put("event", "gameList")
							.put(Constants.GAME, this.games.values().stream().map(GameVerticle::toJson).toList())
							.toString());
		// this.webSocket.addConnetedUser(user, newId);
		// this.vertx.setPeriodic(2000, id -> {
		// // Invia un messaggio a un client specifico (usa un ID di esempio qui)
		// this.webSocket.sendMessageToClient(user.clientID(),
		// new JsonObject().put("message", "Messaggio dal server").toString());
		// System.out.println("Messaggio inviato");
		// });
		jsonGame.put(Constants.GAME_ID, String.valueOf(newId));
		return jsonGame;
	}

	public JsonObject joinGame(final UUID gameID, final User user) {
		final JsonObject jsonJoin = new JsonObject();
		if (this.games.get(gameID) != null) {
			if (this.games.get(gameID).getNumberOfPlayersIn() < this.games.get(gameID).getMaxNumberOfPlayers()) {
				if (this.games.get(gameID).addUser(user)) {
					jsonJoin.put(Constants.JOIN_ATTR, true);
					// this.webSocket.addConnetedUser(user, gameID);
					return jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " joined by " + user.username());
				} else {
					jsonJoin.put(Constants.ALREADY_JOINED, true);
					jsonJoin.put(Constants.ERROR, user.username() + " già presente nella partita " + gameID);
					return jsonJoin.put(Constants.MESSAGE, "Game " + gameID + " already joined by " + user.username());
				}
			}
			jsonJoin.put(Constants.FULL, true);
			jsonJoin.put(Constants.ERROR, "Raggiunto il limite massimo di giocatori nella partita " + gameID);
			return jsonJoin.put(Constants.MESSAGE, "Reached the limit of maximum players in the game " + gameID);
		}
		jsonJoin.put(Constants.NOT_FOUND, false);
		jsonJoin.put(Constants.ERROR, "Game " + gameID + " non trovato ");
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
				try {
					jsonStartGame.put(Constants.START_ATTR, true);
					jsonStartGame.put(Constants.MESSAGE, "The game " + gameID + " can start");
				} catch (final Exception e) {
					jsonStartGame.put(Constants.START_ATTR, false);
					jsonStartGame.put(Constants.ERROR, "Errore nell'avvio del game");
					jsonStartGame.put(Constants.MESSAGE, "Error in starting the game");
				}
				return jsonStartGame;
			} else {
				jsonStartGame.put(Constants.START_ATTR, false);
				jsonStartGame.put(Constants.ERROR, "Team non bilanciati o non tutti i giocatori si sono uniti");
				return jsonStartGame.put(Constants.MESSAGE, "Not all the players are in or the teams are not balanced");
			}
		}
		jsonStartGame.put(Constants.NOT_FOUND, false);
		jsonStartGame.put(Constants.START_ATTR, false);
		jsonStartGame.put(Constants.ERROR, "Game " + gameID + " non trovato");
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
				jsonCanStart.put(Constants.ERROR, "Il game " + gameID + " non può iniziare");
				return jsonCanStart.put(Constants.MESSAGE, "The game " + gameID + " can't start");
			}
		}
		jsonCanStart.put(Constants.NOT_FOUND, false);
		jsonCanStart.put(Constants.ERROR, "Game " + gameID + " non trovato");
		return jsonCanStart.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject canPlayCard(final UUID gameID, final String username, final Card<CardValue, CardSuit> card,
			final Boolean isSuitFinishedByPlayer) {
		final JsonObject jsonPlayCard = new JsonObject();
		if (this.games.get(gameID) != null && this.games.get(gameID).canStart()) {
			final GameVerticle game = this.games.get(gameID);
			game.setIsSuitFinished(isSuitFinishedByPlayer);
			if (CardSuit.NONE.equals(game.getTrump())) {
				jsonPlayCard.put(Constants.PLAY, false);
				jsonPlayCard.put(Constants.ERROR, "La briscola non è stata scelta");
				jsonPlayCard.put(Constants.MESSAGE, "Trump not setted");
				return jsonPlayCard;
			}
		} else {
			jsonPlayCard.put(Constants.NOT_FOUND, false);
			jsonPlayCard.put(Constants.ERROR, "Game " + gameID + " non trovato");
			return jsonPlayCard.put(Constants.PLAY, false);
		}
		return jsonPlayCard;
	}

	public JsonObject playCard(final UUID gameID, final String username, final Card<CardValue, CardSuit> card,
			final Boolean isSuitFinishedByPlayer) {
		final JsonObject jsonPlayCard = new JsonObject();
		// game.canPlayCard(card, username);
		// if (game.canPlayCard(card, username)) {
		final GameVerticle game = this.games.get(gameID);
		final Boolean play = game.addCard(card, username);
		jsonPlayCard.put(Constants.PLAY, play);
		System.out.println("(service), play" + play);
		if (play && game.getLatestTrick().isCompleted()) {
			System.out.println("inside");
			game.getGameSchema().addTrick(game.getCurrentTrick());
			if (this.statisticManager != null)
				this.statisticManager.updateRecordWithTrick(String.valueOf(gameID) + '-' + game.getCurrentState().get() / 10 , game.getCurrentTrick());
			try {
				game.onTrickCompleted(game.getCurrentTrick());
				game.setCurrentTrick(new TrickImpl(game.getMaxNumberOfPlayers(), game.getTrump()));
				game.getTricks().add(game.getCurrentTrick());
				game.incrementCurrentState();
				System.out.println("la seconda, prima = incremeted game service" + game.getCurrentState());
				game.onPlayCard();
				if (game.isRoundEnded()) {
					System.out.println("RoundEnded");
					game.onEndRound();
					game.startNewRound();
					System.out.println("game ended" + game.isGameEnded());
					game.onStartGame();
				}
				System.out.println("incremeted game service" + game.getCurrentState());
			} catch (final Exception e) {
				jsonPlayCard.put(Constants.PLAY, false);
				jsonPlayCard.put(Constants.ERROR, "La presa non è stata completata correttamente");
				jsonPlayCard.put(Constants.MESSAGE, "Failed to complete the trick");
				return jsonPlayCard;
			}
			// } else {
			// jsonPlayCard.put(Constants.ERROR, "can't play card with trick, first card
			// choses the suit of trick: " + game.getCurrentTrick());
			// return jsonPlayCard.put(Constants.PLAY, false);
			// }
		}
		return jsonPlayCard;
	}

	public JsonObject chooseTrump(final UUID gameID, final String cardSuit, final String username) {
		final JsonObject jsonTrump = new JsonObject();
		if (this.games.get(gameID) != null) {
			if (this.games.get(gameID).getPositionByUsername(username) == this.games.get(gameID).getTurn() || DEBUG) {
				CardSuit trump;
				try {
					trump = CardSuit.valueOf(cardSuit);
				} catch (final IllegalArgumentException e) {
					trump = CardSuit.NONE;
				}
				this.games.get(gameID).chooseTrump(trump);
				jsonTrump.put(Constants.MESSAGE, trump + " setted as trump");
				jsonTrump.put(Constants.VALUE, trump);
				if (CardSuit.NONE.equals(trump)) {
					jsonTrump.put(Constants.TRUMP, false);
					jsonTrump.put(Constants.ERROR, "Briscola non valida: " + trump);
					jsonTrump.put(Constants.ILLEGAL_TRUMP, true);
					return jsonTrump;
				}
				jsonTrump.put(Constants.TRUMP, true);
				return jsonTrump;
			} else {
				jsonTrump.put(Constants.TRUMP, false);
				jsonTrump.put(Constants.NOT_ALLOWED, true);
				jsonTrump.put(Constants.ERROR, "Il giocatore " + username + " non è autorizzato a scegliere la briscola");
				return jsonTrump.put(Constants.MESSAGE, "The user " + username + " is not allowed to choose the trump");
			}
		} else {
			jsonTrump.put(Constants.TRUMP, false);
			jsonTrump.put(Constants.NOT_FOUND, false);
			jsonTrump.put(Constants.ERROR, "Game " + gameID + " non trovato");
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

	public JsonObject changeTeam(final UUID gameID, final String username, final String team, final Integer pos) {
		final JsonObject jsonTeam = new JsonObject();
		System.out.println("Service The team is " + team + " and the position is " + pos);
		if (this.games.get(gameID) != null) {
			jsonTeam.put(Constants.TEAM, this.games.get(gameID).changeTeam(username, team, pos));
			return jsonTeam;
		}
		jsonTeam.put(Constants.NOT_FOUND, false);
		jsonTeam.put(Constants.ERROR, "Game " + gameID + " non trovato");
		return jsonTeam.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject getState(final UUID gameID) {
		final JsonObject jsonState = new JsonObject();
		if (this.games.get(gameID) != null) {
			final Trick currentTrick = this.games.get(gameID).getCurrentTrick();
			if (currentTrick == null) {
				jsonState.put(Constants.NOT_FOUND, false);
				jsonState.put(Constants.ERROR, "Stato sconosciuto: presa non trovata");
				return jsonState.put(Constants.MESSAGE, "Trick not found");
			}
			jsonState.put(Constants.MESSAGE, currentTrick.toString());
			return jsonState;
		}
		jsonState.put(Constants.NOT_FOUND, false);
		jsonState.put(Constants.ERROR, "Game " + gameID + " non trovato");
		return jsonState.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject isGameEnded(final UUID gameID) {
		final JsonObject jsonEnd = new JsonObject();
		if (this.games.get(gameID) != null) {
			final Boolean isEnded = this.games.get(gameID).isGameEnded();
			jsonEnd.put(Constants.ENDED, isEnded);
			if (!isEnded)
				jsonEnd.put(Constants.ERROR, "Il game " + gameID + " non è concluso");
			return jsonEnd;
		}
		jsonEnd.put(Constants.ENDED, false);
		jsonEnd.put(Constants.ERROR, "Game " + gameID + " non trovato");
		return jsonEnd.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	public JsonObject makeCall(final UUID gameID, final String call, final String username) {
		final JsonObject jsonCall = new JsonObject();
		if (this.games.get(gameID) != null) {
			final boolean success = this.games.get(gameID).makeCall(Call.fromUppercaseString(call.toUpperCase()),
					username);
			if (!success)
				jsonCall.put(Constants.ERROR, "La chiamata " + call + " non è andata a buon fine");
			return jsonCall.put(Constants.MESSAGE, success);
		}
		jsonCall.put(Constants.NOT_FOUND, false);
		jsonCall.put(Constants.ERROR, "Game " + gameID + " non trovato");
		return jsonCall.put(Constants.MESSAGE, "Game " + gameID + " not found");
	}

	/**@param the id of the new game
	 * @return true if the game has been created, false if the gameId is not found
	 */
	public JsonObject newGame(final UUID gameID) {
		final JsonObject jsonNewGame = new JsonObject();
		if (this.games.get(gameID) != null) {
			final GameVerticle previousGame = this.getGames().get(gameID);
			if (!previousGame.isNewGameCreated()){
				previousGame.setNewGameCreated();
				final JsonObject newGameJson = this.createGame(previousGame.getNumberOfPlayersIn(), previousGame.getUsers().get(0), previousGame.getExpectedScore(), previousGame.getGameMode().name());
				final String newGameID = newGameJson.getString(Constants.GAME_ID);
				final GameVerticle newGame = this.getGames().get(UUID.fromString(newGameID));
				previousGame.getUsers().stream().filter(user -> !user.username().equals(previousGame.getUsers().get(0).username())).forEach(newGame::addUser);
				// newGame.startGame();
				newGame.onStartGame();
				previousGame.onNewGame(newGameID);
				jsonNewGame.put(Constants.MESSAGE, "New game created");
				jsonNewGame.put("newGameID", newGameID);
				return jsonNewGame.put(Constants.NEW_GAME_CREATION, true);
			}
			jsonNewGame.put(Constants.NEW_GAME_CREATION, false);
			jsonNewGame.put(Constants.ERROR, "Nuovo game già creato");
			return jsonNewGame.put(Constants.MESSAGE, "New game already created");
		} 
		jsonNewGame.put(Constants.NOT_FOUND, false);
		jsonNewGame.put(Constants.ERROR, "Game " + gameID + " non trovato");
		return jsonNewGame.put(Constants.MESSAGE, "Game " + gameID + " not found");
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

	public JsonObject getPlayers() {
		final JsonObject jsonPlayers = new JsonObject();

		// this.webSocket.getActiveConnections().keySet().stream().map(clientID ->
		// this.getGames().values().stream().map(GameVerticle::getUsers).flatMap(List::stream).filter(user
		// -> user.clientID().equals(clientID))
		// )
		jsonPlayers.put("inGamePlayers",
				this.getGames().values().stream().map(GameVerticle::getUsers)
						.flatMap(List::stream).map(User::username).collect(Collectors.toList()))
				.put("connected", this.webSocket.getActiveUsers());
		return jsonPlayers;
	}
}
