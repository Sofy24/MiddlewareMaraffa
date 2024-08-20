package game;

import static java.lang.Math.floor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import game.service.User;
import game.utils.Constants;
import game.utils.Pair;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
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
	private int teamPos = 1;
	private final double numberOfTricksInRound;
	private boolean newGameCreated = false;
	private Optional<String> password = Optional.empty();
	private Optional<Team> teamAtTrick = Optional.empty();

	// public GameSchema getGameSchema() {
	// return this.gameSchema0
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
		this.teams.add(new Team(List.of(user), "A", 0, 0));
		this.teams.add(new Team(List.of(), "B", 0, 0));
		this.users.add(user);
		this.gameSchema = new GameSchema(String.valueOf(id) + '-' + this.currentState.get() / 10, CardSuit.NONE);
		this.statisticManager = statisticManager;
		this.webSocket = webSocket;
		if (this.statisticManager != null)
			this.statisticManager.createRecord(this.gameSchema); // TODO andrebbero usati gli UUID ma vediamo se mongo
			// di aiuta con la questione _id
		}
	

	public GameVerticle(final UUID id, final User user, final int numberOfPlayers, final int expectedScore,
			final GameMode gameMode, final String password) {
		this.id = id;
		this.gameMode = gameMode;
		this.expectedScore = expectedScore;
		this.currentScore = new Pair<>(0, 0);
		this.currentState = new AtomicInteger(0);
		this.creatorName = user.username();
		this.numberOfPlayers = numberOfPlayers;
		this.numberOfTricksInRound = floor((float) Constants.NUMBER_OF_CARDS / this.numberOfPlayers);
		this.teams.add(new Team(List.of(user), "A", 0, 0));
		this.teams.add(new Team(List.of(), "B", 0, 0));
		this.users.add(user);
		this.gameSchema = new GameSchema(String.valueOf(id) + '-' + this.currentState.get() / 10, CardSuit.NONE);
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
			final Team currentTeam = this.teams.get(this.teamPos % 2);
			final List<User> updatePlayers = new ArrayList<>(currentTeam.players());
			updatePlayers.add(user);
			this.teams.set(this.teamPos % 2, new Team(updatePlayers, currentTeam.nameOfTeam(), currentTeam.score(), currentTeam.currentScore()));
			LOGGER.info("GAME " + this.id + " joined: " + user.toString());
			this.teamPos += 1;
			return true;
		}
		return false;
	}

	
	/**
	 * @param password of the game
	 */
	public void setPassword(final String password) {
		this.password = Optional.of(password);
	}

	/**
	 * @return true if the password is correct
	 */
	public boolean checkPasword(final String pwd) {
		return !this.password.isPresent() || (this.password.isPresent() && this.password.get().equals(pwd));
	}

	/**
	 * Adds the card if the trick is not completed, otherwise it adds the card to a
	 * new trick and updates the current state
	 * 
	 * @param card to be added to the trick
	 */
	public boolean addCard(final Card<CardValue, CardSuit> card, final String username) {
		if (this.turn >= 0) {
			LOGGER.info("GAME " + this.id + " addCard: " + card.toString() + " by " + username + " suitFinished arrat: " + this.isSuitFinished.toString());
			if (this.canStart() && this.users.get(this.turn).username().equals(username)) {
				if (this.currentTrick == null) {
					this.currentTrick = this.states.getOrDefault(this.currentState.get(),
							new TrickImpl(this.numberOfPlayers, this.trump));
					this.tricks.add(this.currentTrick);
					this.teamAtTrick = this.teams.stream().filter(t -> t.players().stream().map(User::username).toList().contains(this.users.get(this.turn).username())).findFirst();
					LOGGER.info("[Duplicate] Start of a new trick means first player is:" + this.users.get(this.turn).username() + " and the team is: " + this.teamAtTrick.get());
				}

				// if (card.cardValue() == CardValue.ONE && this.checkMaraffa) { //TODO SISTEMARE
				// if (this.checkMaraffa) { //TODO SISTEMARE
				this.onCheckMaraffa(card.cardSuit().value, card.cardValue().value, this.trump.value, username);
				// }
				// this.checkMaraffa = false;

				if (this.currentTrick.getCardsAndUsers().containsValue(username)) {
					return false;
				}

				this.currentTrick.addCard(card, username);
				LOGGER.info("GAME " + this.id + " currentTrick: " + this.currentTrick.toString());
				this.turn = (this.turn + 1) % this.numberOfPlayers;
				this.removeFromHand(card, username);
				LOGGER.info("Checking trick is completed: " + this.currentTrick.isCompleted());
				if (this.currentTrick.isCompleted()) {
					LOGGER.info("Yes it is");
					this.getStates().put(this.getCurrentState().get(), this.getCurrentTrick());
				} else {
					LOGGER.info("NO it isnnnnn");
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
		LOGGER.info("GAME " + this.id + " chose trump: " + suit.toString());
		this.onNewRound(); // simply notify new trump
		if (this.statisticManager != null) {
			this.statisticManager.updateSuit(this.gameSchema); // TODO serve davvero o soltanto roba che sembra utile ?
		}
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
			// final List<User> playerNames = IntStream.range(0, maxPlayers)
			this.users = IntStream.range(0, maxPlayers)
					.mapToObj(i -> this.teams.stream()
							.filter(team -> team.players().size() > i)
							.map(team -> team.players().get(i)))
					.flatMap(Stream::distinct)
					.collect(Collectors.toList());

			// create a map to perform look up between Users and their usernames
			final Map<String, User> userMap = this.users.stream()
					.collect(Collectors.toMap((Function<? super User, ? extends String>) User::username, user -> user));

			// ordering users
			// this.users = playerNames.stream()
			// .map(userMap::get)
			// .collect(Collectors.toList());
			if (this.turn != -1) {
		this.teamAtTrick = this.teams.stream().filter(t -> t.players().stream().map(User::username).toList().contains(this.users.get(this.turn).username())).findFirst();
				LOGGER.info("Start of a new trick means first player is:" + this.users.get(this.turn).username() + " and the team is: " + this.teamAtTrick.get());
			}

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
		LOGGER.info("this.users.stream().map(User::username).toList().get(this.turn).equals(username):  "
				+ this.users.stream().map(User::username).toList().get(this.turn).equals(username));
		if (this.users.stream().map(User::username).toList().get(this.turn).equals(username)) {
			this.currentTrick.setCall(call, username);
			this.onMakeCall(call);
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
		this.teamAtTrick = this.teams.stream().filter(t -> t.players().stream().map(User::username).toList().contains(this.users.get(this.turn).username())).findFirst();
		LOGGER.info("[Set current trick] Start of a new trick means first player is:" + this.users.get(this.turn).username() + " and the team is: " + this.teamAtTrick.get());
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

	public void setInitialTurn(final int initTurn) {
		this.initialTurn = initTurn % this.numberOfPlayers;
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


	public void setScoreAfterMistake(final int score, final boolean isFirstTeam) {
		LOGGER.info("Calling:  setScoreAfterMistake" );
		final String beginTeam = this.teamAtTrick.get().nameOfTeam();
		final int index = isFirstTeam ? 0 : 1;
		final int invIndex = isFirstTeam ? 1 : 0;
		final Team currentTeam = beginTeam == "A" ? this.teams.get(index) : this.teams.get(invIndex);
		final Team invTeam =beginTeam == "A" ? this.teams.get(invIndex) : this.teams.get(index);
		
		LOGGER.info("GAME " + this.id + " turn : " + this.currentState.get());
		LOGGER.info("[Score 11toZero] Score situation: " + this.teams.toString());
		LOGGER.info("[Score 11toZero] Begin team: " + this.teamAtTrick.toString());
		LOGGER.info(
			"[Score 11toZero] MISTAKE made by team: " + currentTeam.nameOfTeam() + " so team :" + invTeam.nameOfTeam() + " wins the round"
		);
		LOGGER.info("mistake being made by anyone beetween: " + currentTeam.players().stream().map(User::username).toList().toString());
		LOGGER.info("[Score 11toZero] Setting: " + this.teams.get(beginTeam == "A" ? index : invIndex).toString() + " as winner" );
		this.teams.set(
				beginTeam == "A" ? index : invIndex,
				new Team(currentTeam.players(), currentTeam.nameOfTeam(), currentTeam.score(), 0));

		LOGGER.info("[Score 11toZero] Setting: " + this.teams.get(beginTeam == "A" ? invIndex : index).toString() + " as loser" );
		this.teams.set(beginTeam == "A" ? invIndex : index, 
				new Team(invTeam.players(), invTeam.nameOfTeam(), invTeam.score() + score , 0));
		LOGGER.info("[Score 11toZero] Score situation: " + this.teams.toString());
		if (this.isGameEnded()) {
			LOGGER.info("Game ended with eleven to zero, somebody is quite a noob...");
			this.onEndGame();
		}
	}
	/**
	 * update the score of the teams
	 *
	 * @param score   of the team who won the trick
	 * @param isTeamA true if team A won the trick
	 */
	public void setScore(final int score, final boolean isTeamA) {
		final int index = isTeamA ? 0 : 1;
		final int invIndex = isTeamA ? 1 : 0;
		Team currentTeam = this.teams.get(index);
		LOGGER.info("GAME " + this.id + " turn : " + this.currentState.get());
		LOGGER.info("Score situation: " + this.teams.toString());
		LOGGER.info(
				"GAME " + this.id + " score before compute: " + currentTeam.nameOfTeam() + " : " + currentTeam.score());
		this.teams.set(index,
				new Team(currentTeam.players(), currentTeam.nameOfTeam(), currentTeam.score(), currentTeam.currentScore() + score));
		// LOGGER.info("GAME " + this.id + " score after compute: " +
		// currentTeam.nameOfTeam() + " : "
		// + (currentTeam.score() + score));
		// LOGGER.info("GAME " + this.id + " teams : " + this.teams.toString());
		Team invTeam = this.teams.get(invIndex);
		currentTeam = this.teams.get(index);
		LOGGER.info("Score situation: " + this.teams.toString());
		// System.out.println("after score: " + currentTeam.score() +
		// currentTeam.nameOfTeam());
		// System.out.println("after score: " + invTeam.score() + invTeam.nameOfTeam());
		if (this.currentState.get() == (int) this.numberOfTricksInRound) { //TODO SOFY qui ho diminuito di 1 ma va bene ????
			// LOGGER.info("GAME " + this.id + " score before ultima presa: " +
			// currentTeam.nameOfTeam() + " : "
			// + currentTeam.score());
			this.teams.set(index,
					new Team(currentTeam.players(), currentTeam.nameOfTeam(),
							currentTeam.score(), (currentTeam.currentScore() - currentTeam.currentScore() % 3) + 3));
			this.teams.set(invIndex,
					new Team(invTeam.players(), invTeam.nameOfTeam(),
							invTeam.score(), (invTeam.currentScore() - invTeam.currentScore() % 3)));
			LOGGER.info("Score situation: " + this.teams.toString());
			currentTeam = this.teams.get(index);
			invTeam = this.teams.get(invIndex);
			this.teams.set(index,
					new Team(currentTeam.players(), currentTeam.nameOfTeam(),
							currentTeam.score() + currentTeam.currentScore(), 0));
			this.teams.set(invIndex,
					new Team(invTeam.players(), invTeam.nameOfTeam(),
							invTeam.score() + invTeam.currentScore(), 0));

			LOGGER.info(
				"The fucking game is done, user: " + this.users.get(this.initialTurn) + " is not your turn anymore !"
			);
			LOGGER.info("Score situation: " + this.teams.toString());
			this.initialTurn += 1;
			this.setInitialTurn(this.initialTurn);
			this.checkMaraffa = true;
			LOGGER.info(
				"I choose you : " + this.users.get(this.initialTurn) + ", pick a trump"
			);
			// System.out.println("after +1 score: " + currentTeam.score());
			// System.out.println("after +1 score, but other team: " + invTeam.score());
			// ??? currentTeam.score() + (currentTeam.score() % 3 == 0 ? 1 :
			// currentTeam.score() % 3)
			// LOGGER.info("GAME " + this.id + " score after ultima presa: " +
			// currentTeam.nameOfTeam() + " : "
			// + currentTeam.score() + (currentTeam.score() % 3 == 0 ? 1 :
			// currentTeam.score() % 3));

			LOGGER.info("GAME " + this.id + " teams : " + this.teams.toString());
		}
	}

	// /**
	//  * update the score of the teams for 11-0 mode
	//  *
	//  * @param isTeamA true if team A committed the mistake
	//  */
	// public void setScore(final boolean isTeamA) {
	// 	int index11 = 1;
	// 	int index0 = 0;
	// 	if (!isTeamA) {
	// 		index11 = 0;
	// 		index0 = 1;
	// 	}
	// 	this.teams.set(index0,
	// 			new Team(this.teams.get(index0).players(), this.teams.get(index0).nameOfTeam(), 0));
	// 	this.teams.set(index11,
	// 			new Team(this.teams.get(index11).players(), this.teams.get(index11).nameOfTeam(),
	// 					Constants.ELEVEN_ZERO_SCORE * 3));
	// }

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
			// final List<User> users = this.teams.stream().flatMap(tm ->
			// tm.players().stream()).toList();
			final User deletedUser = this.teams.stream()
					.flatMap(tm -> tm.players().stream().filter(u -> u.username().equals(username))).findFirst()
					.orElseThrow();
			this.teams = this.teams.stream().map(t -> {
				final List<User> updatedPlayers = new ArrayList<>(t.players());
				// updatedPlayers.remove(updatedPlayers.stream().map(User::username).toList().indexOf(username));
				updatedPlayers.remove(deletedUser);
				return new Team(updatedPlayers, t.nameOfTeam(), t.score(), t.currentScore());
			}).collect(Collectors.toList());
			final Team selectedteam = this.teams.stream().filter(t -> t.nameOfTeam().equals(team)).findFirst()
					.orElseThrow();
			try {
				final int teamIndex = this.teams.indexOf(selectedteam);
				final List<User> updatedPlayers = new ArrayList<>(selectedteam.players());
				updatedPlayers.add(pos, deletedUser);
				this.teams.set(teamIndex, new Team(updatedPlayers, selectedteam.nameOfTeam(), selectedteam.score(), selectedteam.currentScore()));
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

	/** @return the expected score a team should obtain in order to win the game */
	public int getExpectedScore() {
		return this.expectedScore;
	}

	/** @return the flag newGameCreated */
	public boolean isNewGameCreated() {
		return this.newGameCreated;
	}

	/** set the flag newGameCreated to true */
	public void setNewGameCreated() {
		this.newGameCreated = true;
	}

	/**
	 * @return the number of players for this game
	 */
	public int getMaxNumberOfPlayers() {
		return this.numberOfPlayers;
	}

	
	/**
* @return the password of this game,
	 */
	public Optional<String> getPassword() {
		return this.password;
	}

	/**
	 * Set the team who lose the game because of a mistake
	 */
	public void endRoundByMistake(final boolean firstTeam) {
		this.elevenZeroTeam = firstTeam ? 0 : 1;
		LOGGER.info(
				"Some dumbass made a mistake: " + this.users.get(this.initialTurn) + " is not your turn anymore !"
			);
		LOGGER.info(
				"The fucking game is done, user: " + this.users.get(this.initialTurn) + " is not your turn anymore !"
			);
		this.initialTurn += 1;
		this.setInitialTurn(this.initialTurn);
			LOGGER.info(
				"I choose you : " + this.users.get(this.initialTurn) + ", pick a trump"
			);
		// 	LOGGER.info(
		// 		"current state before: " + this.currentState.get()
		// 	);

		// 	this.incrementCurrentState();
		// }
		// 	LOGGER.info(
		// 		"current state after: " + this.currentState.get() 
		// 	);

			this.elevenZeroTeam = -1;
		this.checkMaraffa = true;
	}

	/**
	 * @return true if the round is ended
	 */
	public boolean isRoundEnded() {

		if (this.elevenZeroTeam != -1) {
			this.setCurrentState((int) this.numberOfTricksInRound);
		}
		// if (this.currentState.get() == this.numberOfTricksInRound) {
		// 	this.setInitialTurn(this.initialTurn++);
		// 	this.checkMaraffa = true;
		// }
		LOGGER.info(
				"GAME " + this.id + " currentState : " + this.currentState.get() + " is round ended: "
						+ (this.currentState.get() == (int) this.numberOfTricksInRound) + " turn making trumps:  index(" + this.initialTurn+ ") -> user: " + this.users.get(this.initialTurn));
		return this.currentState.get() == (int) this.numberOfTricksInRound;
	}

	/**
	 * @return true if the game is ended
	 */
	public boolean isGameEnded() {
		System.out.println("endA" + this.teams.get(0).score() / 3 + "endB" + this.teams.get(1).score() / 3);
		return this.teams.get(0).score() / 3 >= this.expectedScore
				|| this.teams.get(1).score() / 3 >= this.expectedScore;
	}

	/**
	 * @param username to be removed
	 */
	public void removeUser(final String username){
		final List<User> usersToRemove = this.users.stream()
		.filter(u -> u.username().equals(username))
		.collect(Collectors.toList());
		this.users.removeAll(usersToRemove);
		final List<Team> updatedTeams = this.teams.stream()
		.map(team -> new Team(team.players().stream()
			.filter(user -> !username.equals(user.username()))
			.collect(Collectors.toList()),
			team.nameOfTeam(),
			team.score(),
			team.currentScore()
		))
		.collect(Collectors.toList());
		this.teams = updatedTeams;
		this.onRemoveUser();
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
				.put("turn", this.turn)
				.put("state", this.currentState.get())
				.put("password", this.password.isPresent())
				.put("trumpSelected", this.trump.toString())
				.put("trumpSelectorUsername", this.users.get(this.initialTurn >= 0 ? this.initialTurn : 0).username())
				.put("teamA", this.teams.get(0))
				.put("teamB", this.teams.get(1))
				.put("trick", this.currentTrick)
				.put("teamAScore", (this.teams.get(0).score() + this.teams.get(0).currentScore()) / 3)
				.put("teamBScore", (this.teams.get(1).score() +this.teams.get(1).currentScore()) / 3)
				.put("teamAScoreCurrent", this.teams.get(0).currentScore() / 3)
				.put("teamBScoreCurrent", this.teams.get(1).currentScore() / 3)
				.put("mode", this.gameMode.toString());
		System.out.println("json" + json.toString());
		return json;
	}

	@Override
	public void onCreateGame(final User user) {
		// if (this.getVertx() != null) {
		// 	this.getVertx().eventBus().request("chat-component:onCreateGame", this.toJson().toString(), reply -> {
		// 		if (reply.succeeded()) {
		// 			System.out.println("created the chat so add the creator");
		// 			this.onJoinGame(user);
		// 		}
		// 	});
		// }
	}

	@Override
	public void onJoinGame(final User user) {
		// if (this.getVertx() != null) {
		// 	this.getVertx().eventBus().send("chat-component:onJoinGame",
		// 			new JsonObject().put("gameID", this.id.toString()).put("username", user.username())
		// 					.put("clientID", user.clientID()).toString());
		// }
		if (this.webSocket != null) {
			for (final var player : this.users) {
				this.webSocket.sendMessageToClient(player.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								.put("event", "userJoin")
								.put("username", user.username())
								.put("status", this.status.toString())
								.put("teamA", this.teams.get(0))
								.put("teamB", this.teams.get(1))
								// .put("teamA", this.teams.get(0).players().stream().map(User::username).toList())
								// .put("teamB", this.teams.get(1).players().stream().map(User::username).toList())
								.put("status", this.status.toString()).toString());
			}
		}

	}

	@Override
	public void onStartGame() {
		if (this.getVertx() != null) {
			this.getVertx().eventBus().request("game-startRound:onStartGame",
					new JsonObject().put(Constants.GAME_ID, this.id.toString())
							.put(Constants.NUMBER_OF_PLAYERS, this.numberOfPlayers).toString(),
					reply -> {
						if (reply.succeeded()) {
							LOGGER.info("The game succeeded in starting");
							if (this.webSocket != null) {
								for (final var user : this.users) {
									System.out.println(this.users.toString());
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

	}

	@Override
	// public void onCheckMaraffa(final int suit, final String username) {
	public void onCheckMaraffa(final int suit, final int value, final int trump, final String username) {
		final int user = this.turn;
		if (this.getVertx() != null) {
			this.getVertx().eventBus().request("game-maraffa:onCheckMaraffa",
					new JsonObject()
							.put(Constants.VALUE, value)
							.put(Constants.SUIT, suit)
							.put(Constants.TRUMP, trump)
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
	}

	@Override
	public void onChangeTeam() {
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								// .put("teamA", this.teams.get(0).players().stream().map(User::username).toList())
								// .put("teamB", this.teams.get(1).players().stream().map(User::username).toList())
								.put("teamA", this.teams.get(0))
								.put("teamB", this.teams.get(1))
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
								.put("latestTrick",
										this.getCurrentState().get() - 1 < this.tricks.size()
												&& this.getCurrentState().get() - 1 >= 0
														? this.tricks.get(this.getCurrentState().get() - 1)
														: null)
				.put("teamAScore", (this.teams.get(0).score() + this.teams.get(0).currentScore()) / 3)
				.put("teamBScore", (this.teams.get(1).score() +this.teams.get(1).currentScore()) / 3)
								.put("userTurn", this.users.get(this.turn).username()).toString());
			}

			System.out.println("PLAY CARD teamAScore" + this.teams.get(0).score() / 3);
			System.out.println("PLAY CARD teamBScore" + this.teams.get(1).score() / 3);
		}
	}

	@Override
	public CompletableFuture<Void> onTrickCompleted(final Trick latestTrick) {
		final CompletableFuture<Void> future = new CompletableFuture<>();
		final int[] cardArray = latestTrick.getCards().stream().mapToInt(Integer::parseInt).toArray();
		System.out.println("the result i want, on trick" + Arrays.toString(cardArray));
		if (this.getVertx() != null) {
			LOGGER.info("Team A cardsSsss: " + Arrays.toString(latestTrick.getCardsAndUsers().entrySet()
							.stream()
							.filter(e -> this.teams.get(0).players().stream().map(User::username).toList()
									.contains(e.getValue()))
							.map(Map.Entry::getKey)
							.mapToInt(Integer::parseInt).toArray())
							);
			this.getVertx().eventBus().request("game-trickCommpleted:onTrickCommpleted", new JsonObject()
					.put(Constants.GAME_ID, this.id.toString())
					.put(Constants.TRICK, Arrays.toString(cardArray))
					.put("userList", new Gson().toJson(this.currentTrick.getCardsAndUsers()))
					.put(Constants.GAME_MODE, this.gameMode.toString())
					.put(Constants.IS_SUIT_FINISHED, this.getIsSuitFinished().toString())
					.put("turn", (this.turn + 1) % this.numberOfPlayers)
					.put("teamACards", latestTrick.getCardsAndUsers().entrySet()
							.stream()
							.filter(e -> this.teams.get(0).players().stream().map(User::username).toList()
									.contains(e.getValue()))
							.map(Map.Entry::getKey)
.mapToInt(Integer::parseInt).toArray()

					)
					.put(Constants.TRUMP, this.trump.getValue()).toString(), reply -> {
						System.out.println("succe" + reply.succeeded());
						System.out.println("reply" + reply.toString());
						if (reply.succeeded()) {
							future.complete(null);
							if (reply.result() == null) {
								System.out.println("NON VA BENE QUA");
							}
							this.onPlayCard();
							this.clearIsSuitFinished();
							if (this.isGameEnded()) {
								System.out.println("GameEnded");
								this.onEndGame();
							}
						} else {
							throw new UnsupportedOperationException("Failed to complete the trick");
						}
					});
		}
		return future;
	}

	@Override
	public void onMessage() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onMessage'");
	}

	@Override
	public void onEndRound() {
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								.put("event", "endRound")
								.put("teamA", this.teams.get(0).players().stream().map(User::username).toList())
								.put("teamB", this.teams.get(1).players().stream().map(User::username).toList())
								.put("trumpSelectorUsername",
										this.users.get(this.initialTurn >= 0 ? this.initialTurn : 0).username())
				.put("teamAScore", (this.teams.get(0).score() + this.teams.get(0).currentScore()) / 3)
				.put("teamBScore", (this.teams.get(1).score() +this.teams.get(1).currentScore()) / 3)
								.toString());

			}
		}
	}

	@Override
	public void onEndGame() {
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								.put("event", "endGame")
								.put("teamA", this.teams.get(0).players().stream().map(User::username).toList())
								.put("teamB", this.teams.get(1).players().stream().map(User::username).toList())
				.put("teamAScore", (this.teams.get(0).score() + this.teams.get(0).currentScore()) / 3)
				.put("teamBScore", (this.teams.get(1).score() +this.teams.get(1).currentScore()) / 3)
								.toString());
			}
		}
		// if (this.vertx != null)
		// this.vertx.eventBus().send("user-component", this.toJson().toString());
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
	public void onMakeCall(final Call call) {
		if (this.webSocket != null) {
			for (final var player : this.users) {
				if (player != this.getUsers().get(this.turn)) {
					this.webSocket.sendMessageToClient(player.clientID(),
							new JsonObject().put("gameID", this.id.toString())
									.put("event", "call")
									.put("username", this.getUsers().get(this.turn).username())
									.put("call", call.toString()).toString());

				}
			}
		}
	}

	@Override
	public void onNewRound() {
		this.gameSchema.setGameID(String.valueOf(this.id) + '-' + this.currentState.get() / 10);
		this.gameSchema.setTrump(CardSuit.NONE);
		if (this.statisticManager != null) {
			this.statisticManager.createRecord(this.gameSchema);
		}
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

	@Override
	public void onNewGame(final String newGameID) {
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								.put("event", "newGame")
								.put("newGameID", newGameID).toString());
			}
		}
	}

	public void setTurnWithUser(final String username) {
		this.turn = this.users.stream().map(User::username).toList().indexOf(username);
	}

	@Override
	public void onRemoveUser() {
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject().put("gameID", this.id.toString())
								.put("event", "userRemoved").toString());
			}
		}
	}

	public void messageReceived(final String msg, final String type, final UUID gameID, final String author) {
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject()
								.put("event", "onMessage")
								.put("message", msg)
								.put("author", author)
								.toString());
			}
		}
	}

	public void canPlayCard(final Card<CardValue, CardSuit> card, final String username) {
		// vertx must be, play card asks the business logic
		this.getVertx().eventBus().request("game-playCard:validate",
				new JsonObject()
						.put("card", card)
						.put(Constants.GAME_ID, this.id.toString())
						.put("isCardTrump", card.cardSuit().equals(this.trump))
						.put(Constants.USERNAME, username)
						.toString(),
				reply -> {
					if (reply.succeeded()) {
						if ((Boolean) reply.result().body()) {
							LOGGER.info("You have played a valid card");
							// return (Boolean) reply.result().body();
						}
						LOGGER.info("The game succeeded in checking Maraffa");
					} else {
						throw new UnsupportedOperationException("Failed to check Maraffa");
					}
				});
	}

	public void onExitGame() {
		LOGGER.info("game " + this.id + " is exiting");
		if (this.webSocket != null) {
			for (final var user : this.users) {
				this.webSocket.sendMessageToClient(user.clientID(),
						new JsonObject()
								.put("event", "exitGame").toString());
			}
			this.webSocket.broadcastToEveryone(new JsonObject()
			.put("event", "gameRemoved").toString());
		}
		this.vertx.setTimer(5000, event -> {
			try {
				this.stop();
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		// if (this.vertx != null)
		// this.vertx.eventBus().send("user-component", this.toJson().toString());
	}

}
