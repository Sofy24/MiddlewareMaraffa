package game.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import BLManagment.BusinessLogicController;
import game.Card;
import game.CardSuit;
import game.CardValue;
import game.GameMode;
import game.GameVerticle;
import game.service.schema.CanStartResponse;
import game.service.schema.ChangeTeamBody;
import game.service.schema.ChooseTrumpBody;
import game.service.schema.CreateGameBody;
import game.service.schema.GetGamesResponse;
import game.service.schema.IsRoundEndedResponse;
import game.service.schema.JoinGameBody;
import game.service.schema.MakeCallBody;
import game.service.schema.NewGameBody;
import game.service.schema.PasswordBody;
import game.service.schema.PlayCardBody;
import game.service.schema.RemoveUserBody;
import game.service.schema.StartBody;
import game.service.schema.StateResponse;
import game.utils.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import repository.AbstractStatisticManager;
import server.WebSocketVertx;

/**
 * This class is a decorator for a game service.
 */
public class GameServiceDecorator {
	private final Map<UUID, GameVerticle> games = new ConcurrentHashMap<>();
	private final GameService gameService;
	private final BusinessLogicController businessLogicController;
	private static final Logger LOGGER = LoggerFactory.getLogger(GameServiceDecorator.class);
	private final WebSocketVertx webSocket;
	// private final static Boolean DEBUG = true;
	// final static Dotenv dotenv = Dotenv.configure().load();

	public GameServiceDecorator(final Vertx vertx, final AbstractStatisticManager statisticManager,
			final WebSocketVertx webSocket) {
		this.gameService = new GameService(vertx, statisticManager, webSocket);
		this.webSocket = webSocket;
		this.businessLogicController = new BusinessLogicController(vertx, this.gameService,
				Integer.parseInt(System.getenv().getOrDefault("BUSINESS_LOGIC_PORT", "3000")),
				System.getenv().getOrDefault("BUSINESS_LOGIC_HOST", "localhost"));

	}

	@Operation(summary = "Create new game", method = Constants.CREATE_GAME_METHOD, operationId = Constants.CREATE_GAME, tags = {
			Constants.GAME_TAG}, requestBody = @RequestBody(description = "insert username and the number of players", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = CreateGameBody.class, example = "{\n"
					+ "  \"" + Constants.NUMBER_OF_PLAYERS + "\": 4,\n" + "  \"" + Constants.USERNAME
					+ "\": \"sofi\",\n" + "  \"" + Constants.EXPECTED_SCORE + "\": 41,\n" + "  \"" + Constants.GAME_MODE
					+ "\": \"CLASSIC\",\n"
					// TODO check perche scompare tutto
					+ " \"" + Constants.GUIID + "\": \"c1bdcf34-e0f2-409c-aced-e00d4be32b00\"\n" + "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game-creation", implementation = CreateGameBody.class))),
							@ApiResponse(responseCode = "417", description = "Invalid game mode."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void createGame(final RoutingContext context) {
		final String guiIdAsString = context.body().asJsonObject().getString(Constants.GUIID);
		final UUID guiId = UUID.fromString(guiIdAsString);
		final Integer numberOfPlayers = context.body().asJsonObject().getInteger(Constants.NUMBER_OF_PLAYERS);
		final String username = context.body().asJsonObject().getString(Constants.USERNAME);
		final String gameMode = context.body().asJsonObject().getString(Constants.GAME_MODE);
		final boolean isGuest = context.body().asJsonObject().getBoolean(Constants.GUEST, false);
		final Integer expectedScore = context.body().asJsonObject().getInteger(Constants.EXPECTED_SCORE);
		final JsonObject jsonGame = this.gameService.createGame(numberOfPlayers, new User(username, guiId, isGuest),
				expectedScore, gameMode);
		if (jsonGame.containsKey(Constants.INVALID)) {
			context.response().setStatusCode(401).end("Invalid game mode");
		}
		context.response().end(jsonGame.toBuffer());
	}

	@Operation(summary = "Join a specific game", method = Constants.JOIN_GAME_METHOD, operationId = Constants.JOIN_GAME, tags = {
			Constants.GAME_TAG}, requestBody = @RequestBody(description = "username and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = JoinGameBody.class, example = "{\n"
					// TODO check qui c'e'il doppio campo, sara' per le maiuscole
					// ?
					+ "  \"" + Constants.GUIID + "\": \"c1bdcf34-e0f2-409c-aced-e00d4be32b00\",\n" + "  \""
					+ Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" + "  \""
					+ Constants.PASSWORD + "\": \"\",\n" + "  \""
					+ Constants.USERNAME + "\": \"james\"\n" + "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = JoinGameBody.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "417", description = "Reached the limit of maximum players in the game."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void joinGame(final RoutingContext context) {
		final String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
		final String guiIdAsString = context.body().asJsonObject().getString(Constants.GUIID);
		final boolean isGuest = context.body().asJsonObject().getBoolean(Constants.GUEST, false);
		final UUID gameID = UUID.fromString(uuidAsString);
		final UUID guiId = UUID.fromString(guiIdAsString);
		final String pwd = context.body().asJsonObject().getString(Constants.PASSWORD);
		final String username = context.body().asJsonObject().getString(Constants.USERNAME);
		final JsonObject joinResponse = this.gameService.joinGame(gameID, new User(username, guiId, isGuest), pwd);
		if (joinResponse.containsKey(Constants.NOT_FOUND)) {
			context.response().setStatusCode(404).end(joinResponse.getString(Constants.MESSAGE));
		} else if (joinResponse.containsKey(Constants.FULL)) {
			context.response().setStatusCode(401).end(joinResponse.getString(Constants.MESSAGE));
		} else {
			context.response().end(joinResponse.toBuffer());
		}
	}

	@Operation(summary = "Start a specific game", method = Constants.START_GAME_METHOD, operationId = Constants.START_GAME, tags = {
			Constants.GAME_TAG}, requestBody = @RequestBody(description = "id of the game is required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = StartBody.class, example = "{\n"
					+ "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\"\n"
					+ "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = StartBody.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void startGame(final RoutingContext context) {
		final String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		final JsonObject startResponse = this.gameService.startGame(gameID);
		if (!startResponse.containsKey(Constants.NOT_FOUND)) {
			context.response().end(startResponse.toBuffer());
		} else {
			context.response().setStatusCode(404).end(startResponse.toBuffer());
		}
	}

	@Operation(summary = "Start a new round in a specific game", method = Constants.START_NEW_ROUND_METHOD, operationId = Constants.START_NEW_ROUND, tags = {
			Constants.ROUND_TAG}, requestBody = @RequestBody(description = "id of the game is required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = StartBody.class, example = "{\n"
					+ "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\"\n"
					+ "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = StartBody.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void startNewRound(final RoutingContext context) {
		final String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		if (this.gameService.startNewRound(gameID)) {
			context.response().end("New round started");
		} else {
			context.response().setStatusCode(404).end("Game " + gameID + " not found");
		}
	}

	@Operation(summary = "User change team in a specific game", method = Constants.CHANGE_TEAM_METHOD, operationId = Constants.CHANGE_TEAM, tags = {
			Constants.GAME_TAG}, requestBody = @RequestBody(description = "id of the game, username, team and position are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = ChangeTeamBody.class, example = "{\n"
					+ "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n"
					+ "  \"" + Constants.TEAM + "\": \"A\",\n"
					+ "  \"" + Constants.POSITION + "\": 0,\n"
					+ "  \"" + Constants.USERNAME + "\": \"sofi\"\n"
					+ "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = ChangeTeamBody.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "417", description = "Game already started."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void changeTeam(final RoutingContext context) {
		final String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		final String username = context.body().asJsonObject().getString(Constants.USERNAME);
		final String team = context.body().asJsonObject().getString(Constants.TEAM);
		final Integer position = context.body().asJsonObject().getInteger(Constants.POSITION);
		final JsonObject teamResponse = this.gameService.changeTeam(gameID, username, team, position);
		if (!teamResponse.containsKey(Constants.NOT_FOUND)) {
			if (teamResponse.getBoolean(Constants.TEAM)) {
				context.response().end(new JsonObject().put(Constants.MESSAGE, "Team changed").toBuffer());
			} else {
				context.response().setStatusCode(417)
						.end(new JsonObject().put(Constants.MESSAGE, "The game is already started").toBuffer());
			}
		} else {
			context.response().setStatusCode(404)
					.end(new JsonObject().put(Constants.MESSAGE, "Game " + gameID + " not found").toBuffer());
		}
	}

	@Operation(summary = "Get player card", method = Constants.GET_PLAYER_CARD_METHOD, operationId = Constants.PLAYER_CARDS, tags = {
			Constants.GAME_TAG}, parameters = {
					@Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string")),
					@Parameter(in = ParameterIn.PATH, name = Constants.USERNAME, required = true, description = "The unique user name", schema = @Schema(type = "string"))}, responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = StartBody.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void getPlayerCard(final RoutingContext context) {
		final UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
		final String username = context.pathParam(Constants.USERNAME);
		if (this.gameService.getGames().containsKey(gameID)) {
			context.response().end(new JsonObject()
					.put("cards", this.gameService.getGames().get(gameID).getUserCards(username)).toBuffer());
		} else {
			context.response().setStatusCode(404).end("Game " + gameID + " not found");
		}
	}

	@Operation(summary = "Check if a round can start", method = Constants.CAN_START_METHOD, operationId = Constants.CAN_START, tags = {
			Constants.ROUND_TAG}, parameters = {
					@Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))}, responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = CanStartResponse.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void canStart(final RoutingContext context) {
		final UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
		final JsonObject startResponse = this.gameService.canStart(gameID);
		if (!startResponse.containsKey(Constants.NOT_FOUND)) {
			// TODO implement canStart
			context.response().end(startResponse.toBuffer());
		} else {
			context.response().setStatusCode(404).end(startResponse.toBuffer());
		}
	}

	@Operation(summary = "A player plays a card in a specific game", method = Constants.PLAY_CARD_METHOD, operationId = Constants.PLAY_CARD, tags = {
			Constants.ROUND_TAG}, requestBody = @RequestBody(description = "username, card and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = PlayCardBody.class, example = "{\n"
					+ "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n"
					+ "  \"" + Constants.USERNAME + "\": \"sofi\",\n"
					+ "  \"" + Constants.CARD_VALUE + "\": \"ONE\",\n"
					+ "  \"" + Constants.IS_SUIT_FINISHED + "\": \"true\",\n"
					+ "  \"" + Constants.CARD_SUIT + "\": \"COINS\"\n" + "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = PlayCardBody.class))),
							@ApiResponse(responseCode = "404", description = "Game or username not found."),
							@ApiResponse(responseCode = "417", description = "Is not the turn of this player."),
							@ApiResponse(responseCode = "401", description = "Invalid username or card."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void playCard(final RoutingContext context) {
		final JsonObject response = new JsonObject();
		final String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		final String cardValue = context.body().asJsonObject().getString(Constants.CARD_VALUE);
		final String cardSuit = context.body().asJsonObject().getString(Constants.CARD_SUIT);
		final Boolean isSuitFinishedByPlayer = context.body().asJsonObject().getBoolean(Constants.IS_SUIT_FINISHED);
		try {
			final Card<CardValue, CardSuit> card = new Card<>(CardValue.getName(cardValue), CardSuit.getName(cardSuit));
			final String username = String.valueOf(context.body().asJsonObject().getValue(Constants.USERNAME));
			final int userPosition = this.gameService.getGames().get(gameID).getPositionByUsername(username);
			if (userPosition == -1) {
				response.put(Constants.MESSAGE, "Invalid user " + username);
				context.response().setStatusCode(401).end(response.toBuffer());
				return;
			}
			if (CardSuit.NONE.equals(card.cardSuit()) || CardValue.NONE.equals(card.cardValue())) {
				response.put(Constants.MESSAGE, "Invalid card " + card);
				context.response().setStatusCode(401).end(response.toBuffer());
			} else {
				final JsonObject canPlayCardResponse = this.gameService.canPlayCard(gameID, username, card,
						isSuitFinishedByPlayer);
				if (!canPlayCardResponse.containsKey(Constants.NOT_FOUND)) {
					final Boolean isCardTrump = this.gameService.getGames().get(gameID).getTrump()
							.equals(CardSuit.getName(cardSuit));
					final int[] trick = this.gameService.getGames().get(gameID).getCurrentTrick() == null ? new int[0]
							: this.gameService.getGames().get(gameID).getCurrentTrick().getCards().stream()
									.mapToInt(Integer::parseInt).toArray();
					final int[] userCards = this.gameService.getGames().get(gameID).getUserCards(username).stream()
							.mapToInt(userCard -> userCard.getCardValue().intValue()).toArray();
					this.businessLogicController.validatePlayCard(trick, card.getCardValue(), userCards, isCardTrump)
							.whenComplete((res, err) -> {
								if (err == null) {
									if (GameMode.ELEVEN2ZERO
											.equals(this.gameService.getGames().get(gameID).getGameMode())
											|| res.getBoolean("valid")) {
										final JsonObject playCardResponse = this.gameService.playCard(gameID, username,
												card,
												isSuitFinishedByPlayer);
										if (!this.gameService.getGames().get(gameID).isUserIn(username)
												|| !playCardResponse.getBoolean(Constants.PLAY)) {
											response.put(Constants.ERROR, "Non è il turno di " + username
													+ " o non è stata scelta la briscola o i team non sono bilanciati o il sistema non sa chi ha il 4 di denari");
											response.put(Constants.MESSAGE, "Is not the turn of " + username
													+ " or the trump is not setted or the teams are not balanced or the system doesn't know who has the 4 of coins");
											context.response().setStatusCode(417).end(response.toBuffer());
										} else {
											System.out.println("answer by decorator = current state "
													+ this.gameService.getGames().get(gameID).getCurrentState());
											System.out.println("answer by decorator = turn "
													+ this.gameService.getGames().get(gameID).getTurn());
											System.out.println("answer by decorator = currenttrick "
													+ this.gameService.getGames().get(gameID).getCurrentTrick());
											context.response().end(response.toBuffer());
										}
									} else {
										response.put(Constants.ERROR, "Carta non valida");
										response.put(Constants.MESSAGE, "Invalid card " + card);
										context.response().setStatusCode(500).end(response.toBuffer());
									}
								}
							});
				} else {
					response.put(Constants.ERROR, "Id del game " + gameID + " non trovato");
					response.put(Constants.MESSAGE, "Game " + gameID + " not found");
					context.response().setStatusCode(404).end(response.toBuffer());
				}
			}
		} catch (final IllegalArgumentException e) {
			response.put(Constants.MESSAGE, "Errore nel giocare la carta: " + cardValue + " di " + cardSuit);
			context.response().setStatusCode(417).end(response.toBuffer());
		}

	}

	@Operation(summary = "Choose the trump", method = Constants.CHOOSE_TRUMP_METHOD, operationId = Constants.CHOOSE_TRUMP, tags = {
			Constants.ROUND_TAG}, requestBody = @RequestBody(description = "username, trump and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = ChooseTrumpBody.class, example = "{\n"
					+ "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n"
					+ "  \"" + Constants.USERNAME + "\": \"sofi\",\n"
					+ "  \"" + Constants.CARD_SUIT + "\": \"COINS\"\n"
					+ "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = ChooseTrumpBody.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "417", description = "Invalid suit or user not allowed."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void chooseTrump(final RoutingContext context) {
		final String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		final String cardSuit = context.body().asJsonObject().getString(Constants.CARD_SUIT);
		final String username = context.body().asJsonObject().getString(Constants.USERNAME);
		final JsonObject trumpResponse = this.gameService.chooseTrump(gameID, cardSuit, username);
		if (!trumpResponse.containsKey(Constants.NOT_FOUND) && !trumpResponse.containsKey(Constants.ILLEGAL_TRUMP)
				&& !trumpResponse.containsKey(Constants.NOT_ALLOWED)) {
			context.response().end(trumpResponse.toBuffer());
		} else if (trumpResponse.containsKey(Constants.ILLEGAL_TRUMP)) {
			context.response().setStatusCode(401).end(trumpResponse.toBuffer());
		} else if (trumpResponse.containsKey(Constants.NOT_ALLOWED)) {
			context.response().setStatusCode(417).end(trumpResponse.toBuffer());
		} else {
			context.response().setStatusCode(404).end(trumpResponse.toBuffer());
		}
	}

	@Operation(summary = "Get the state of a specific game", method = Constants.STATE_METHOD, operationId = Constants.STATE, // !
			// operationId
			// must
			// be
			// the
			// same
			// as
			// controller
			tags = {Constants.GAME_TAG}, parameters = {
					@Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))}, responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = StateResponse.class))),
							@ApiResponse(responseCode = "404", description = "Game or trick not found."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void getState(final RoutingContext context) {
		final UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
		final String message = this.gameService.getState(gameID).getString(Constants.MESSAGE);
		if (this.gameService.getGames().get(gameID).canStart()) {
			context.response().end(message);
		} else {
			context.response().setStatusCode(404).end(message);
		}
	}

	@Operation(summary = "Check if the game is ended", method = Constants.END_METHOD, operationId = Constants.END_GAME, // !
			// operationId
			// must
			// be
			// the
			// same
			// as
			// controller
			tags = {Constants.GAME_TAG}, parameters = {
					@Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))}, responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = IsRoundEndedResponse.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void isGameEnded(final RoutingContext context) {
		final UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
		final JsonObject jsonEnd = this.gameService.isGameEnded(gameID);
		if (!jsonEnd.containsKey(Constants.NOT_FOUND)) {
			context.response().end(jsonEnd.toBuffer());
		} else {
			context.response().setStatusCode(404).end(jsonEnd.toBuffer());
		}
	}

	@Operation(summary = "Make a call", method = Constants.MAKE_CALL_METHOD, operationId = Constants.MAKE_CALL, tags = {
			Constants.ROUND_TAG}, requestBody = @RequestBody(description = "call, username and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = MakeCallBody.class, example = "{\n"
					+ "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" + "  \""
					+ Constants.CALL + "\": \"string\",\n" + "  \"" + Constants.USERNAME + "\": \"string\"\n"
					+ "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = MakeCallBody.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "417", description = "Invalid call."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void makeCall(final RoutingContext context) {
		final String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		final String call = context.body().asJsonObject().getString(Constants.CALL);
		final String username = context.body().asJsonObject().getString(Constants.USERNAME);
		final JsonObject jsonCall = this.gameService.makeCall(gameID, call, username);
		if (!jsonCall.containsKey(Constants.NOT_FOUND)) {
			if (jsonCall.getBoolean(Constants.MESSAGE)) {
				context.response().end("Call " + call + " setted!");
			} else {
				context.response().setStatusCode(404).end("Invalid call");
			}
		} else {
			context.response().setStatusCode(404).end(jsonCall.getString(Constants.MESSAGE));
		}
	}

	@Operation(summary = "Get all the games", method = Constants.GAMES_METHOD, operationId = Constants.GAMES, // !
			// operationId
			// must
			// be
			// the
			// same
			// as
			// controller
			tags = {Constants.GAME_TAG}, responses = {
					@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = GetGamesResponse.class))),
					@ApiResponse(responseCode = "404", description = "Game not found."),
					@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void getGames(final RoutingContext context) {
		final JsonArray jsonGetGames = this.gameService.getJsonGames();
		if (!jsonGetGames.isEmpty()) {
			context.response().end(jsonGetGames.toBuffer());
		} else {
			// jsonGetGames.add(Constants.NOT_FOUND);
			context.response().setStatusCode(404).end(jsonGetGames.toBuffer());
			// context.response().setStatusCode(404).end(new
			// JsonObject().put(Constants.MESSAGE, Constants.NOT_FOUND).toBuffer());
		}
	}

	@Operation(summary = "Get all the players", method = Constants.PLAYERS_METHOD, operationId = Constants.GET_PLAYERS, // !
			// operationId
			// must
			// be
			// the
			// same
			// as
			// controller
			tags = {Constants.GAME_TAG}, responses = {
					@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = GetGamesResponse.class))),
					@ApiResponse(responseCode = "404", description = "Game not found."),
					@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void getPlayers(final RoutingContext context) {
		context.response().end(this.gameService.getPlayers().toBuffer());

	}

	@Operation(summary = "Get a specific game", method = Constants.GAMES_METHOD, operationId = Constants.GETGAME, // !
			tags = {Constants.GAME_TAG}, parameters = {
					@Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))}, responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = GetGamesResponse.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void getGame(final RoutingContext context) {
		final UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
		final JsonObject jsonGetGames = this.gameService.getGames().get(gameID).toJson();
		if (!jsonGetGames.isEmpty()) {
			context.response().end(jsonGetGames.toBuffer());
		} else {
			// jsonGetGames.add(Constants.NOT_FOUND);
			context.response().setStatusCode(404).end(jsonGetGames.toBuffer());
			// context.response().setStatusCode(404).end(new
			// JsonObject().put(Constants.MESSAGE, Constants.NOT_FOUND).toBuffer());
		}
	}

	@Operation(summary = "Create new game", method = Constants.NEW_GAME_METHOD, operationId = Constants.NEW_GAME, tags = {
			Constants.GAME_TAG}, requestBody = @RequestBody(description = "the id of the previous game is required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = NewGameBody.class, example = "{\n"
					+ " \"" + Constants.GAME_ID + "\": \"c1bdcf34-e0f2-409c-aced-e00d4be32b00\"\n"
					+ "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "new-game-creation", implementation = NewGameBody.class))),
							@ApiResponse(responseCode = "404", description = "Game not found."),
							@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void newGame(final RoutingContext context) {
		final String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		final JsonObject jsonGame = this.gameService.newGame(gameID);
		System.out.println("new game decorator");
		if (!jsonGame.containsKey(Constants.NOT_FOUND)) {
			if (jsonGame.getBoolean(Constants.NEW_GAME_CREATION)) {
				context.response().end(jsonGame.getString(Constants.MESSAGE));
			} else {
				context.response().end(jsonGame.getString(Constants.MESSAGE));
			}
		} else {
			context.response().setStatusCode(404).end(jsonGame.getString(Constants.MESSAGE));
		}
	}

	@Operation(summary = "Set the password of a specific game", method = Constants.PASSOWRD_METHOD, operationId = Constants.SET_PASSWORD, tags = {
		Constants.GAME_TAG }, requestBody = @RequestBody(description = "id of the game is required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = PasswordBody.class, example = "{\n"
				+ "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n"
				+ "  \"" + Constants.PASSWORD + "\": \"string\"\n"
				+ "}"))), responses = {
						@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = PasswordBody.class))),
						@ApiResponse(responseCode = "404", description = "Game not found."),
						@ApiResponse(responseCode = "500", description = "Internal Server Error.") })
	public void setPassword(final RoutingContext context) {
		final String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		final String password = context.body().asJsonObject().getString(Constants.PASSWORD);
		final boolean setted = this.gameService.setPassword(gameID, password);
		if (setted) {
			context.response().end(new JsonObject().put(Constants.PASSWORD, "Password impostata").toBuffer());
		} else
			context.response().setStatusCode(404).end(new JsonObject().put(Constants.ERROR, "Gioco non trovato").toBuffer());
	}

	@Operation(summary = "Exit the game", method = Constants.EXIT_GAME, operationId = Constants.GETGAME, tags = {
			Constants.GAME_TAG}, responses = {
					@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "new-game-creation", implementation = NewGameBody.class))),
					@ApiResponse(responseCode = "404", description = "Game not found."),
					@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void exitGame(final RoutingContext context) {
		final UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
		final JsonObject jsonGame = this.gameService.exitGame(gameID);
		System.out.println("new game decorator");
		if (!jsonGame.containsKey(Constants.NOT_FOUND)) {
			context.response().end();
		} else {
			context.response().setStatusCode(404).end(jsonGame.getString(Constants.ERROR));
		}

	}

	@Operation(summary = "Remove a specific user of a specific game", method = Constants.REMOVE_USER_METHOD, operationId = Constants.REMOVE_USER, tags = {
		// Constants.GAME_TAG}, requestBody = @RequestBody(description = "username and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = RemoveUserBody.class, example = "{\n" + "  \""
		// 		+ Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" + "  \""
		// 		+ Constants.USERNAME + "\": \"sofi\"\n" + "}"))), responses = {
			Constants.GAME_TAG}, requestBody = @RequestBody(description = "username and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = RemoveUserBody.class, example = "{\n"
			+ "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n"
			+ "  \"" + Constants.USERNAME + "\": \"sofi\"\n"
			+ "}"))), responses = {
						@ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = RemoveUserBody.class))),
						@ApiResponse(responseCode = "404", description = "Game not found."),
						@ApiResponse(responseCode = "500", description = "Internal Server Error.")})
	public void removeUser(final RoutingContext context) {
		final String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
		final UUID gameID = UUID.fromString(uuidAsString);
		final String username = context.body().asJsonObject().getString(Constants.USERNAME);
		final JsonObject jsonGame = this.gameService.removeUser(gameID, username);
		if (!jsonGame.containsKey(Constants.NOT_FOUND)) {
			context.response().end();
		} else {
			context.response().setStatusCode(404).end(jsonGame.getString(Constants.ERROR));
		}

	}

	public Map<UUID, GameVerticle> getGames() {
		return this.games;
	}
}
