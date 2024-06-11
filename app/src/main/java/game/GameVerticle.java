package game;

import static java.lang.Math.floor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.gson.Gson;

import game.service.User;
import game.utils.Constants;
import game.utils.Pair;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import repository.AbstractStatisticManager;
import server.WebSocketVertx;

/***
 * This class models a game using a Verticle from vertx.
 * id = the id of the verticle
 * numberOfPlayers = the numbers of players of this game
 * stateMap = it saves each state with the related trick
 * users = it keeps track of all the users added to the game
 */
public class GameVerticle extends AbstractVerticle implements IGameAgent {
	private final UUID id;
	private AtomicInteger currentState;
	private final int numberOfPlayers;
	private final Pair<Integer, Integer> currentScore;
	private final int expectedScore;
	private CardSuit trump = CardSuit.NONE;
	private Map<Integer, Trick> states = new ConcurrentHashMap<>();
	private List<User> users = new ArrayList<>();
	private final Map<User, List<Card<CardValue, CardSuit>>> userAndCards = new ConcurrentHashMap<>();
	private final GameSchema gameSchema;
	private AbstractStatisticManager statisticManager;
	private Trick currentTrick;
	private final List<Trick> tricks = new ArrayList<>();
	private List<Team> teams = new ArrayList<>();
	private final String creatorName;
	private Boolean checkMaraffa = true;
	private Status status = Status.WAITING_PLAYERS;
	private final GameMode gameMode;
	private int turn = -1;
	private int initialTurn = -1;
	private List<Boolean> isSuitFinished = new ArrayList<>();
	private WebSocketVertx webSocket;
	private int elevenZeroTeam = -1;
	private double numberOfTricksInRound;

	// public GameSchema getGameSchema() {
	// return this.gameSchema;
	// }
	private static final Logger LOGGER = LoggerFactory.getLogger(GameVerticle.class);

	public GameVerticle(final UUID id, final User user, final int numberOfPlayers, final int expectedScore,
			final GameMode gameMode,
			final AbstractStatisticManager statisticManager, final WebSocketVertx webSocket) {
		this.id = id;
		this.gameMode = gameMode;
		this.expectedScore = expectedScore;
		this.currentScore = new Pair<>(0, 0);
		this.currentState = new AtomicInteger(0);
		this.numberOfPlayers = numberOfPlayers;
		this.numberOfTricksInRound = floor((float) Constants.NUMBER_OF_CARDS / this.numberOfPlayers);
		this.creatorName = user.username();
		this.teams.add(new Team(List.of(this.creatorName), "A", 0));
		this.teams.add(new Team(List.of(), "B", 0));
		this.users.add(user);
		this.gameSchema = new GameSchema(String.valueOf(id), CardSuit.NONE);
		this.statisticManager = statisticManager;
		this.webSocket = webSocket;
		if (this.statisticManager != null)
			this.statisticManager.createRecord(this.gameSchema); // TODO andrebbero usati gli UUID ma vediamo se mongo
		// di aiuta con la questione _id
	}

	public GameVerticle(final UUID id, final User user, final int numberOfPlayers, final int expectedScore,
			final GameMode gameMode) {
		this.id = id;
		this.gameMode = gameMode;
		this.expectedScore = expectedScore;
		this.currentScore = new Pair<>(0, 0);
		this.currentState = new AtomicInteger(0);
		this.creatorName = user.username();
		this.numberOfPlayers = numberOfPlayers;
		this.numberOfTricksInRound = floor((float) Constants.NUMBER_OF_CARDS / this.numberOfPlayers);
		this.teams.add(new Team(List.of(this.creatorName), "A", 0));
		this.teams.add(new Team(List.of(), "B", 0));
		this.users.add(user);
		this.gameSchema = new GameSchema(String.valueOf(id), CardSuit.NONE);
	}

	/**
	 * It starts the verticle
	 */
	@Override
	public void start(final Promise<Void> startPromise) {
		startPromise.complete();
	}

	/**
	 * @return true if the user is added
	 */
	public boolean addUser(final User user) {
		if (!this.users.stream().map(User::username).toList().contains(user.username())) {
			this.users.add(user);
			this.status = this.canStart() ? Status.STARTING : Status.WAITING_PLAYERS;
			this.onJoinGame(user);
			final List<String> updatePlayers = new ArrayList<>(this.teams.get(0).players());
			updatePlayers.add(user.username());
			this.teams.set(0, new Team(updatePlayers, "A", this.teams.get(0).score()));
			return true;
		}
		return false;
	}

	/**
	 * Adds the card if the trick is not completed, otherwise it adds the card to a
	 * new trick and updates the current state
	 * 
	 * @param card to be added to the trick
	 */
	public boolean addCard(final Card<CardValue, CardSuit> card, final String username) {
		if (this.turn >= 0) {
			if (this.canStart() && this.users.get(this.turn).username().equals(username)) {
				if (this.currentTrick == null) {
					this.currentTrick = this.states.getOrDefault(this.currentState.get(),
							new TrickImpl(this.numberOfPlayers, this.trump));
					this.tricks.add(this.currentTrick);
				}

				if (card.cardValue() == CardValue.ONE && this.checkMaraffa) {
					this.onCheckMaraffa(card.cardSuit().value, username);
				}
				this.checkMaraffa = false;

				if (this.currentTrick.getCardsAndUsers().containsValue(username)) {
					return false;
				}
				this.currentTrick.addCard(card, username);
				System.out.println("card" + card.toString());
				System.out.println(this.currentTrick.toString());
				this.turn = (this.turn + 1) % this.numberOfPlayers;
				this.removeFromHand(card, username);
				if (this.currentTrick.isCompleted()) {
					this.getStates().put(this.getCurrentState().get(), this.getCurrentTrick());
				} else {
					this.onPlayCard();
				}
				return true;
			}
		}
		return false;
	}

	private void removeFromHand(final Card<CardValue, CardSuit> card, final String username) {
		this.userAndCards.entrySet().stream()
				.filter(e -> e.getKey().username().equals(username))
				.findFirst()
				.ifPresent(e -> {
					final List<Card<CardValue, CardSuit>> updateCards = new ArrayList<>(e.getValue());
					updateCards.remove(card);
					this.userAndCards.put(e.getKey(), Collections.unmodifiableList(updateCards));
				});

	}

	/**
	 * @return true if the teams are balanced: have the same number of players
	 * 
	 */
	public boolean balancedTeams() {
		return this.teams.stream()
				.mapToInt(team -> team.players().size())
				.distinct()
				.count() == 1;
	}

	/**
	 * @return true if all players have joined the game and if the teams are
	 *         balanced
	 */
	public boolean canStart() {
		return this.users.size() == this.numberOfPlayers;
	}

	/**
	 * @param suit the leading suit of the round
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
		if (this.canStart() && this.balancedTeams()) {
			// get number of players
			final int maxPlayers = this.teams.stream()
					.mapToInt(team -> team.players().size())
					.max()
					.orElse(0);
			// get an ordered list of Usernames
			final List<String> playerNames = IntStream.range(0, maxPlayers)
					.mapToObj(i -> this.teams.stream()
							.filter(team -> team.players().size() > i)
							.map(team -> team.players().get(i)))
					.flatMap(Stream::distinct)
					.collect(Collectors.toList());

			// create a map to perform look up between Users and their usernames
			final Map<String, User> userMap = this.users.stream()
					.collect(Collectors.toMap((Function<? super User, ? extends String>) User::username, user -> user));

			// ordering users
			this.users = playerNames.stream()
					.map(userMap::get)
					.collect(Collectors.toList());

			this.status = Status.PLAYING;
			this.onStartGame();
			return true;
		}
		return false;
	}

	/**
	 * reset the trump
	 */
	public void startNewRound() {
		this.chooseTrump(CardSuit.NONE);
		this.elevenZeroTeam = -1;
	}

	/**
	 * @param call     the call
	 * @param username the user who makes the call
	 * @return true if the call is made correctly
	 */
	public boolean makeCall(final Call call, final String username) {
		if (this.currentTrick == null) {
			this.currentTrick = this.states.getOrDefault(this.currentState.get(),
					new TrickImpl(this.numberOfPlayers, this.trump));
		}
		if (this.users.stream().map(User::username).toList().get(this.turn).equals(username)) {
			this.currentTrick.setCall(call, username);
		}
		return !Call.NONE.equals(this.currentTrick.getCall());
	}

	public UUID getId() {
		return this.id;
	}

	public GameSchema getGameSchema() {
		return this.gameSchema;
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

	/**
	 * @param value
	 */
	public void setCurrentState(final int value) {
		this.currentState = new AtomicInteger(value);

	}

	public Trick getCurrentTrick() {
		return this.currentTrick;
	}

	public void setCurrentTrick(final Trick trick) {
		this.currentTrick = trick;
	}

	public Trick getLatestTrick() {
		System.out.println("tricks" + this.tricks);
		System.out.println("currentState" + this.getCurrentState().get());
		final Trick latestTrick = this.tricks.get(this.getCurrentState().get());
		return latestTrick;
	}

	public List<Trick> getTricks() {
		return this.tricks;
	}

	public int getInitialTurn() {
		return this.initialTurn;
	}

	public void setInitialTurn(final int initialTurn) {
		this.initialTurn = initialTurn % this.numberOfPlayers;
		this.turn = this.initialTurn;
	}

	public int getTurn() {
		return this.turn;
	}

	public void setTurn(final int turn) {
		this.turn = turn;
	}

	public List<User> getUsers() {
		return this.users;
	}

	public int getPositionByUsername(final String username) {
		return this.users.stream().map((Function<? super User, ? extends String>) User::username).toList()
				.indexOf(username);
	}

	public List<Boolean> getIsSuitFinished() {
		return this.isSuitFinished;
	}

	public void clearIsSuitFinished() {
		this.isSuitFinished = new ArrayList<>();
	}

	/*
	 * @param value: true if the user has finished the suit
	 * the values in isSuitFinished are order by playCard
	 * (if Fede plays first, then the first value in the list is the one of Fede)
	 * 
	 * @return true if the value is setted
	 */
	public boolean setIsSuitFinished(final Boolean value) {
		if (this.isSuitFinished.size() == this.numberOfPlayers) {
			this.isSuitFinished = new ArrayList<>();
		}
		try {
			this.isSuitFinished.add(value);
		} catch (final IndexOutOfBoundsException e) {
			return false;
		}
		return true;
	}

	/**
	 * update the score of the teams
	 *
	 * @param score   of the team who won the trick
	 * @param isTeamA true if team A won the trick
	 */
	public void setScore(final int score, final boolean isTeamA) {
		final int index = isTeamA ? 0 : 1;
		final Team currentTeam = this.teams.get(index);
		this.teams.set(index,
				new Team(currentTeam.players(), currentTeam.nameOfTeam(), currentTeam.score() + (score / 3)));
		if (this.currentState.get() == numberOfTricksInRound) {
			this.teams.set(index,
				new Team(currentTeam.players(), currentTeam.nameOfTeam(), currentTeam.score() + 1));
		}
	}

	/**
	 * update the score of the teams for 11-0 mode
	 *
	 * @param isTeamA true if team A committed the mistake
	 */
	public void setScore(final boolean isTeamA) {
		int index11 = 1;
		int index0 = 0;
		if (!isTeamA) {
			index11 = 0;
			index0 = 1;
		}
		this.teams.set(index0,
				new Team(this.teams.get(index0).players(), this.teams.get(index0).nameOfTeam(), 0));
		this.teams.set(index11,
				new Team(this.teams.get(index11).players(), this.teams.get(index11).nameOfTeam(),
						Constants.ELEVEN_ZERO_SCORE));
	}

	public CardSuit getTrump() {
		return this.trump;
	}

	public Status getStatus() {
		return this.status;
	}

	public List<Card<CardValue, CardSuit>> getUserCards(final String username) {
		return this.userAndCards.entrySet().stream()
				.filter(e -> e.getKey().username().equals(username))
				.findFirst()
				.map(Map.Entry::getValue)
				.map(cards -> cards.stream().sorted((o1, o2) -> o1.getCardValue().compareTo(o2.getCardValue()))
						.collect(Collectors.toList()))
				.orElse(Collections.emptyList());
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

	/**
	 * increment the current state
	 */
	public void incrementCurrentState() {
		this.currentState.incrementAndGet();
	}

	/**
	 * a player change a team
	 */
	public boolean changeTeam(final String username, final String team, final Integer pos) {
		System.out.println("Change team: The team is " + team + " and the position is " + pos);
		if (this.status == Status.WAITING_PLAYERS || this.status == Status.STARTING) {
			this.teams = this.teams.stream().map(t -> {
				final List<String> updatedPlayers = new ArrayList<>(t.players());
				updatedPlayers.remove(username);
				return new Team(updatedPlayers, t.nameOfTeam(), t.score());
			}).collect(Collectors.toList());
			final Team selectedteam = this.teams.stream().filter(t -> t.nameOfTeam().equals(team)).findFirst()
					.orElseThrow();
			try {
				final int teamIndex = this.teams.indexOf(selectedteam);
				final List<String> updatedPlayers = new ArrayList<>(selectedteam.players());
				updatedPlayers.add(pos, username);
				this.teams.set(teamIndex, new Team(updatedPlayers, selectedteam.nameOfTeam(), selectedteam.score()));
				LOGGER.info("The team has been changed" + this.teams.toString());
				this.onChangeTeam();
				return true;
			} catch (final IndexOutOfBoundsException e) {
				throw new IndexOutOfBoundsException("Cannot add a user, the team is too small");
			}
		}
		return false;
	}

	/**
	 * @return true if the user is in the game
	 */
	public boolean isUserIn(final String user) {
		return this.users.stream().map(User::username).toList().contains(user);
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
	 * Set the team who lose the game because of a mistake
	 */
	public void endRoundByMistake(final boolean firstTeam) {
		this.elevenZeroTeam = firstTeam ? 0 : 1;
	}

	/**
	 * @return true if the round is ended
	 */
	public boolean isRoundEnded() {
		
		if (this.elevenZeroTeam != -1) {
			this.setCurrentState((int) numberOfTricksInRound);
		}
		if (this.currentState.get() == numberOfTricksInRound) {
			this.setInitialTurn(this.initialTurn++);
			this.checkMaraffa = true;
		}
		return this.currentState.get() == numberOfTricksInRound;
	}

	/**
	 * @return true if the game is ended
	 */
	public boolean isGameEnded() {
		return this.teams.get(0).score() >= this.expectedScore || this.teams.get(1).score() >= this.expectedScore;
	}

	/**
	 * @return a json with id, status and game mode
	 */
	public JsonObject toJson() {
		final JsonObject json = new JsonObject();
		json.put("gameID", this.id.toString())
				.put("creator", this.creatorName)
				.put("status", this.status.toString())
				.put("score", this.expectedScore)
				.put("firstPlayer", this.users.get(this.turn >= 0 ? this.turn : 0).username())
				.put("playerTurn", this.users.get(this.turn >= 0 ? this.turn : 0).username())
				.put("state", this.currentState.get())
				.put("trumpSelected", this.trump.toString())
				.put("trumpSelectorUsername", this.users.get(this.initialTurn >= 0 ? this.initialTurn : 0).username())
				.put("teamA", this.teams.get(0).players())
				.put("teamB", this.teams.get(1).players())
				.put("trick", this.currentTrick)
				.put("teamAScore", this.teams.get(0).score())
				.put("teamBScore", this.teams.get(1).score())
				.put("mode", this.gameMode.toString());
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
		if (this.webSocket != null) {
			for (final var player : this.users) {
				this.webSocket.sendMessageToClient(player.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								.put("event", "userJoin")
								.put("username", user.username())
								.put("status", this.status.toString())
								.put("teamA", this.teams.get(0).players())
								.put("teamB", this.teams.get(1).players())
								.put("status", this.status.toString()).toString());
			}
		}

	}

	@Override
	public void onStartGame() {
		if (this.getVertx() != null)
			this.getVertx().eventBus().request("game-startRound:onStartGame",
					new JsonObject().put(Constants.GAME_ID, this.id.toString())
							.put(Constants.NUMBER_OF_PLAYERS, this.numberOfPlayers).toString(),
					reply -> {
						if (reply.succeeded()) {
							LOGGER.info("The game succeeded in starting");
							if (this.webSocket != null) {
								for (final var user : this.users) {
									this.webSocket.sendMessageToClient(user.clientID(),
											new JsonObject()
													.put("event", "startGame")
													.put("firstPlayer", this.users.get(this.turn).username())
													.put("gameID", this.id.toString())
													.toString());
								}
								// this.webSocket.sendMessageToClient(this.users.get(this.turn).clientID(),
								// new JsonObject().put("gameID", this.id.toString())
								// .put("event", "userTurn")
								// .put("turn", this.turn)
								// .put("userTurn", this.users.get(this.turn).username()).toString());
							}
						} else {
							throw new UnsupportedOperationException("Failed to start");
						}
					});

	}

	@Override
	public void onCheckMaraffa(final int suit, final String username) {
		final int user = this.turn;
		if (this.getVertx() != null)
			this.getVertx().eventBus().request("game-maraffs:onCheckMaraffa",
					new JsonObject()
							.put(Constants.SUIT, suit)
							.put(Constants.GAME_ID, this.id.toString())
							.put(Constants.USERNAME, username)
							.toString(),
					reply -> {
						if (reply.succeeded()) {
							if ((Boolean) reply.result().body()) {
								this.setScore(Constants.MARAFFA_SCORE, user % 2 == 0);
								LOGGER.info("You have Maraffa");
							}
							LOGGER.info("The game succeeded in checking Maraffa");
						} else {
							throw new UnsupportedOperationException("Failed to check Maraffa");
						}
					});
	}

	@Override
	public void onChangeTeam() {
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								.put("teamA", this.teams.get(0).players())
								.put("teamB", this.teams.get(1).players())
								.put("event", "changeTeam").toString());
			}
		}
	}

	@Override
	public void onPlayCard() {
		// Websocket
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								.put("event", "userTurn")
								.put("turn", this.turn)
								.put("trick", this.currentTrick)
								.put("teamAScore", this.teams.get(0).score())
								.put("teamBScore", this.teams.get(1).score())
								.put("userTurn", this.users.get(this.turn).username()).toString());

			}

			// this.webSocket.sendMessageToClient(this.users.get(this.turn).clientID(),
			// new JsonObject().put("gameID", this.id.toString())
			// .put("event", "userTurn")
			// .put("turn", this.turn)
			// .put("userTurn", this.users.get(this.turn).username()).toString());
		}
	}

	@Override
	public void onTrickCompleted(final Trick latestTrick) {
		final int[] cardArray = latestTrick.getCards().stream().mapToInt(Integer::parseInt).toArray();
		System.out.println("the result i want, on trick" + Arrays.toString(cardArray));
		if (this.getVertx() != null)
			this.getVertx().eventBus().request("game-trickCommpleted:onTrickCommpleted", new JsonObject()
					.put(Constants.GAME_ID, this.id.toString())
					.put(Constants.TRICK, Arrays.toString(cardArray))
					.put("userList", new Gson().toJson(this.currentTrick.getCardsAndUsers()))
					.put(Constants.GAME_MODE, this.gameMode.toString())
					.put(Constants.IS_SUIT_FINISHED, this.getIsSuitFinished().toString())
					.put(Constants.TRUMP, this.trump.getValue()).toString(), reply -> {
						System.out.println("succe" + reply.succeeded());
						System.out.println("reply" + reply.toString());
						if (reply.succeeded()) {
							if (reply.result() == null) {
								System.out.println("NON VA BENE QUA");
							}
							this.onPlayCard();
							this.clearIsSuitFinished();
						} else {
							throw new UnsupportedOperationException("Failed to complete the trick");
						}
					});
	}

	@Override
	public void onMessage() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onMessage'");
	}

	@Override
	public void onEndRound() {
		if (this.vertx != null)
			this.vertx.eventBus().send("user-component", this.toJson().toString());
	}

	public void handOutCards(final List<Integer> list) {
		final int chunkSize = (list.size() + this.numberOfPlayers - 1) / this.numberOfPlayers;
		int index = 0;
		for (final var integer : IntStream.range(0, this.numberOfPlayers)
				.mapToObj(i -> list.subList(i * chunkSize, Math.min(list.size(), (i + 1) * chunkSize)))
				.collect(Collectors.toList())) {
			this.userAndCards.put(this.users.get(index++),
					integer.stream().map(Card::fromInteger).toList());
		}
	}

	@Override
	public void onNewRound() {
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject()
								.put("event", "trumpEvent")
								.put("username", this.users.get(this.initialTurn).username())
								.put("trumpSelected", this.trump.toString())
								.toString());
			}
			// this.webSocket.sendMessageToClient(this.users.get(this.turn).clientID(),
			// new JsonObject().put("gameID", this.id.toString())
			// .put("event", "userTurn")
			// .put("turn", this.turn)
			// .put("userTurn", this.users.get(this.turn).username()).toString());
		}
	}

	public void setTurnWithUser(final String username) {
		this.turn = this.users.stream().map(User::username).toList().indexOf(username);
	}

}
