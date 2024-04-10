package game;

import static java.lang.Math.floor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import game.service.User;
import game.utils.Constants;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import repository.AbstractStatisticManager;

/***
 * This class models a game using a Verticle from vertx. id = the id of the
 * verticle numberOfPlayers = the numbers of players of this game stateMap = it
 * saves each state with the related trick users = it keeps track of all the
 * users added to the game
 */
public class GameVerticle extends AbstractVerticle implements IGameAgent {
	private final UUID id;
	private final AtomicInteger currentState;
	private final int numberOfPlayers;
	// private Pair<Integer, Integer> currentScore;
	private final int expectedScore;
	private CardSuit trump = CardSuit.NONE;
	private Map<Integer, Trick> states = new ConcurrentHashMap<>();
	private final List<User> users = new ArrayList<>();
	private final GameSchema gameSchema;
	private AbstractStatisticManager statisticManager;
	private Trick currentTrick;
	private final List<Trick> tricks = new ArrayList<>();
	private Team team1;
	private Team team2;
	private Status status = Status.WAITING_PLAYERS;
	private final GameMode gameMode;

	public GameSchema getGameSchema() {
		return this.gameSchema;
	}

	public GameVerticle(final UUID id, final User user, final int numberOfPlayers, final int expectedScore,
			final GameMode gameMode,
			final AbstractStatisticManager statisticManager) {
		this.id = id;
		this.gameMode = gameMode;
		this.expectedScore = expectedScore;
		// this.currentScore = new Pair<>(0, 0);
		this.currentState = new AtomicInteger(0);
		this.numberOfPlayers = numberOfPlayers;
		this.users.add(user);
		this.gameSchema = new GameSchema(String.valueOf(id), CardSuit.NONE);
		this.statisticManager = statisticManager;
		if (this.statisticManager != null)
			this.statisticManager.createRecord(this.gameSchema); // TODO andrebbero usati gli UUID ma vediamo se mongo
		// di aiuta con la questione _id
	}

	public GameVerticle(final UUID id, final User user, final int numberOfPlayers, final int expectedScore,
			final GameMode gameMode) {
		this.id = id;
		this.gameMode = gameMode;
		this.expectedScore = expectedScore;
		// this.currentScore = new Pair<>(0, 0);
		this.currentState = new AtomicInteger(0);
		this.numberOfPlayers = numberOfPlayers;
		this.users.add(user);
		this.gameSchema = new GameSchema(String.valueOf(id), CardSuit.NONE);
	}

	/** It starts the verticle */
	@Override
	public void start(final Promise<Void> startPromise) {
		startPromise.complete();
	}

	/**
	 * @return true if the user is added
	 */
	public boolean addUser(final User user) {
		if (!this.users.contains(user.username())) {
			this.users.add(user);
			this.status = this.canStart() ? Status.STARTING : Status.WAITING_PLAYERS;
			return true;
		}
		return false;
	}

	/**
	 * Adds the card if the trick is not completed, otherwise it adds the card to a
	 * new trick and updates the current state
	 *
	 * @param card
	 *             to be added to the trick
	 */
	public boolean addCard(final Card<CardValue, CardSuit> card, final String username) {
		if (this.canStart()) {
			if (this.currentTrick == null) {
				this.currentTrick = this.states.getOrDefault(this.currentState.get(),
						new TrickImpl(this.numberOfPlayers, this.trump));
				this.tricks.add(this.currentTrick);
			}
			this.currentTrick.addCard(card, username);
			if (this.currentTrick.isCompleted()) {
				this.gameSchema.addTrick(this.currentTrick);
				if (this.statisticManager != null)
					this.statisticManager.updateRecordWithTrick(String.valueOf(this.id), this.currentTrick);
				this.states.put(this.currentState.get(), this.currentTrick);
				this.currentTrick = new TrickImpl(this.numberOfPlayers, this.trump);
				this.tricks.add(this.currentTrick);
			}
			return true;
		}
		return false;
	}

	/**
	 * @return true if all players have joined the game
	 */
	public boolean canStart() {
		return this.users.size() == this.numberOfPlayers;
	}

	/**
	 * @param suit
	 *             the leading suit of the round
	 */
	public void chooseTrump(final CardSuit suit) {
		this.trump = suit;
		this.gameSchema.setTrump(suit);
		if (this.statisticManager != null)
			this.statisticManager.updateSuit(this.gameSchema); // TODO serve davvero o soltanto roba che sembra utile ?
	}

	/**
	 * @return true if all the players are in
	 */
	public boolean startGame() {
		if (this.canStart()) {
			this.team1 = new Team(
					IntStream.range(0, this.numberOfPlayers).filter(n -> n % 2 == 0).mapToObj(this.users::get)
							.map(User::username).toList(),
					"A", 0);
			this.team2 = new Team(
					IntStream.range(0, this.numberOfPlayers).filter(n -> n % 2 != 0).mapToObj(this.users::get)
							.map(User::username).toList(),
					"B", 0);
			this.status = Status.PLAYING;
			return true;
		}
		return false;
	}

	/** reset the trump */
	public void startNewRound() {
		this.chooseTrump(CardSuit.NONE);
	}

	/**
	 * @param call
	 *                 the call
	 * @param username
	 *                 the user who makes the call
	 * @return true if the call is made correctly
	 */
	public boolean makeCall(final Call call, final String username) {
		if (this.currentTrick == null) {
			this.currentTrick = this.states.getOrDefault(this.currentState.get(),
					new TrickImpl(this.numberOfPlayers, this.trump));
		}
		if (this.users.get(0).equals(username)) {
			this.currentTrick.setCall(call, username);
		}
		return !Call.NONE.equals(this.currentTrick.getCall());
	}

	public UUID getId() {
		return this.id;
	}

	public Map<Integer, Trick> getStates() {
		return this.states;
	}

	private void setStates(final Map<Integer, Trick> states) {
		this.states = states;
	}

	public AtomicInteger getCurrentState() {
		return this.currentState;
	}

	public Trick getCurrentTrick() {
		return this.currentTrick;
	}

	public Trick getLatestTrick() {
		return this.tricks.get(this.getCurrentState().get());
	}

	/**
	 * update the score of the teams
	 *
	 * @param score
	 *                of the team who won the trick
	 * @param isTeamA
	 *                true if team A won the trick
	 */
	public void setScore(final int score, final boolean isTeamA) {
		if (isTeamA)
			this.team1 = new Team(this.team1.players(), this.team1.nameOfTeam(), this.team1.score() + (score / 3));
		else
			this.team2 = new Team(this.team2.players(), this.team2.nameOfTeam(), this.team2.score() + (score / 3));
		// this.currentScore = isTeamA ? new Pair<>(this.currentScore.getX() + (score /
		// 3), this.currentScore.getY()) : new Pair<>(this.currentScore.getX(),
		// this.currentScore.getY() + (score / 3));
	}

	public CardSuit getTrump() {
		return this.trump;
	}

	public Status getStatus() {
		return this.status;
	}

	/**
	 * @return true if the current trick is completed
	 */
	public boolean isCompleted() {
		return this.currentTrick.isCompleted();
	}

	public GameMode getGameMode() {
		return this.gameMode;
	}

	/** increment the current state */
	public void incrementCurrentState() {
		this.currentState.incrementAndGet();
	}

	/**
	 * @return true if the user is in the game
	 */
	public boolean isUserIn(final String user) {
		return this.users.contains(user);
	}

	/**
	 * @return the number of players who have already joined the game
	 */
	public int getNumberOfPlayersIn() {
		return this.users.size();
	}

	/**
	 * @return the number of players for this game
	 */
	public int getMaxNumberOfPlayers() {
		return this.numberOfPlayers;
	}

	/**
	 * @return true if the round is ended
	 */
	public boolean isRoundEnded() {
		final double numberOfTricksInRound = floor((float) Constants.NUMBER_OF_CARDS / this.numberOfPlayers);
		if (this.currentState.get() == numberOfTricksInRound) {
			this.vertx.eventBus().send("user-component", this.toJson().toString());
			return true;
		}
		return false;
	}

	/**
	 * @return true if the game is ended
	 */
	public boolean isGameEnded() {
		return this.team1.score() >= this.expectedScore || this.team2.score() >= this.expectedScore;
	}

	// TODO e se facessimo una sottoclasse apposta ???
	/**
	 * @return a json with id, status and game mode
	 */
	public JsonObject toJson() {
		final JsonObject json = new JsonObject();
		json.put("gameID", this.id.toString()).put("status", this.status.toString())
				.put("gameMode", this.gameMode.toString()).put("numberOfPlayers", this.numberOfPlayers)
				.put("team1", this.team1).put("team2", this.team2);
		return json;
	}

	@Override
	public void onCreateGame(final User user) {
		if (this.getVertx() != null) {
			this.getVertx().eventBus().request("chat-component:onCreateGame", this.toJson().toString(), reply -> {
				if (reply.succeeded()) {
					System.out.println("created the chat so add the creator");
					this.onJoinGame(user);
				}
			});
		}
	}

	@Override
	public void onJoinGame(final User user) {
		if (this.getVertx() != null)
			this.getVertx().eventBus().send("chat-component:onJoinGame",
					new JsonObject().put("gameID", this.id.toString()).put("username", user.username())
							.put("clientID", user.clientID()).toString());
	}

	@Override
	public void onStartGame() {
		throw new UnsupportedOperationException("Unimplemented method 'onStartGame'");
	}

	@Override
	public void onPlayCard() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onPlayCard'");
	}

	@Override
	public void onMessage() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onMessage'");
	}

	@Override
	public void onEndRound() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onEndRound'");
	}
}
