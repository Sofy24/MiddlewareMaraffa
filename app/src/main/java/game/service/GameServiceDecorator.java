package game.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import server.AppServer;
import game.*;
import httpRest.BusinessLogicController;
import repository.AbstractStatisticManager;
import game.service.schema.CanStartResponse;
import game.service.schema.ChooseTrumpBody;
import game.service.schema.CreateGameBody;
import game.service.schema.GetGamesResponse;
import game.service.schema.IsRoundEndedResponse;
import game.service.schema.JoinGameBody;
import game.service.schema.MakeCallBody;
import game.service.schema.PlayCardBody;
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
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class GameServiceDecorator {
    private final Map<UUID, GameVerticle> games = new ConcurrentHashMap<>();
    private final GameService gameService;
    private BusinessLogicController businessLogicController;
    private static final Logger LOGGER = LoggerFactory.getLogger(AppServer.class);

    public GameServiceDecorator(Vertx vertx) {
        this.gameService = new GameService(vertx);
        this.businessLogicController = new BusinessLogicController(vertx);
    }

    public GameServiceDecorator(Vertx vertx, AbstractStatisticManager statisticManager) {
        this.gameService = new GameService(vertx, statisticManager);
        this.businessLogicController = new BusinessLogicController(vertx);
    }

    @Operation(summary = "Create new game", method = Constants.CREATE_GAME_METHOD, operationId = Constants.CREATE_GAME, tags = {
            Constants.GAME_TAG}, requestBody = @RequestBody(description = "insert username and the number of players", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = CreateGameBody.class, example = "{\n"
            +
            "  \"" + Constants.NUMBER_OF_PLAYERS + "\": 4,\n" +
            "  \"" + Constants.USERNAME + "\": \"sofi\",\n" +
            "  \"" + Constants.EXPECTED_SCORE + "\": 41,\n" +
            "  \"" + Constants.GAME_MODE + "\": \"CLASSIC\"\n" +
            "}"))), responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game-creation", implementation = CreateGameBody.class))),
            @ApiResponse(responseCode = "417", description = "Invalid game mode."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void createGame(RoutingContext context) {
        Integer numberOfPlayers = context.body().asJsonObject().getInteger(Constants.NUMBER_OF_PLAYERS);
        String username = context.body().asJsonObject().getString(Constants.USERNAME);
        String gameMode = context.body().asJsonObject().getString(Constants.GAME_MODE);
        Integer expectedScore = context.body().asJsonObject().getInteger(Constants.EXPECTED_SCORE);
        JsonObject jsonGame = this.gameService.createGame(numberOfPlayers, username, expectedScore, gameMode);
        if (jsonGame.containsKey(Constants.INVALID)) {
            context.response().setStatusCode(401).end("Invalid game mode");
        }
        context.response().end(jsonGame.toBuffer());
    }

    @Operation(summary = "Join a specific game", method = Constants.JOIN_GAME_METHOD, operationId = Constants.JOIN_GAME, tags = {
            Constants.GAME_TAG}, requestBody = @RequestBody(description = "username and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = JoinGameBody.class, example = "{\n"
            +
            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
            "  \"" + Constants.USERNAME + "\": \"james\"\n" +
            "}"))), responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = JoinGameBody.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "417", description = "Reached the limit of maximum players in the game."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void joinGame(RoutingContext context) {
        String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String username = context.body().asJsonObject().getString(Constants.USERNAME);
        JsonObject joinResponse = this.gameService.joinGame(gameID, username);
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
            +
            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\"\n" +
            "}"))), responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = StartBody.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void startGame(RoutingContext context) {
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        JsonObject startResponse = this.gameService.startGame(gameID);
        String message = startResponse.getString(Constants.MESSAGE);
        if (!startResponse.containsKey(Constants.NOT_FOUND)) {
            context.response().end(message);
        }
        context.response().setStatusCode(404).end(message);
    }

    @Operation(summary = "Check if a round can start", method = Constants.CAN_START_METHOD, operationId = Constants.CAN_START, // !
            // operationId
            // must
            // be
            // the
            // same
            // as
            // controller
            tags = {Constants.ROUND_TAG}, parameters = {
            @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = CanStartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void canStart(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        JsonObject startResponse = this.gameService.canStart(gameID);
        System.out.println("Checking deck");
        if (!startResponse.containsKey(Constants.NOT_FOUND)) {
            this.businessLogicController.getShuffledDeck(4).whenComplete((result, err) -> {
                System.out.println("Got sample response");
                System.out.println(result);
                if (result.containsKey("error")) {
                    context.response().setStatusCode(500).end(result.toBuffer());
                } else {
                    JsonArray deck = result.getJsonArray("deck");
                    Integer firstPlayer = result.getInteger("firstPlayer");
                    startResponse.put("deck", deck);
                    startResponse.put("firstPlayer", firstPlayer);
                    context.response().end(startResponse.toBuffer());
                }
            });
        } else {
            context.response().setStatusCode(404).end(startResponse.toBuffer());
        }

    }

    @Operation(summary = "A player plays a card in a specific game", method = Constants.PLAY_CARD_METHOD, operationId = Constants.PLAY_CARD, tags = {
            Constants.ROUND_TAG}, requestBody = @RequestBody(description = "username, card and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = PlayCardBody.class, example = "{\n"
            +
            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
            "  \"" + Constants.USERNAME + "\": \"sofi\",\n" +
            "  \"" + Constants.CARD_VALUE + "\": \"ONE\",\n" +
            "  \"" + Constants.CARD_SUIT + "\": \"COINS\"\n" +
            "}"))), responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = PlayCardBody.class))),
            @ApiResponse(responseCode = "404", description = "Game or username not found."),
            @ApiResponse(responseCode = "417", description = "Invalid card."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void playCard(RoutingContext context) {
        JsonObject response = new JsonObject();
        String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String cardValue = context.body().asJsonObject().getString(Constants.CARD_VALUE);
        String cardSuit = context.body().asJsonObject().getString(Constants.CARD_SUIT);
        try {
            Card<CardValue, CardSuit> card = new Card<>(CardValue.getName(cardValue), CardSuit.getName(cardSuit));
            String username = String.valueOf(context.body().asJsonObject().getValue(Constants.USERNAME));
            if (card.cardSuit().equals(CardSuit.NONE) || card.cardValue().equals(CardValue.NONE)) {
                context.response().setStatusCode(401).end("Invalid " + card);
            } else {
                if (this.gameService.playCard(gameID, username, card)) {
                    if (!this.gameService.getGames().get(gameID).isUserIn(username)) {
                        context.response().setStatusCode(401).end("Invalid user " + username);
                    } else {
                        Trick latestTrick = this.gameService.getGames().get(gameID).getLatestTrick();
                        if (latestTrick.isCompleted()) {
                            this.gameService.getGames().get(gameID).incrementCurrentState();
                            this.businessLogicController.computeScore(latestTrick, this.gameService.getGames().get(gameID).getTrump().getValue().toString()).whenComplete((result, err) -> {
                                LOGGER.info("Got sample response");
                                response.put("result", result);
                                this.gameService.getGames().get(gameID).setScore(result.getInteger("score"), result.getBoolean("firstTeam"));//TODO i punti della businesslogic sono moltiplicati per 3
                                context.response().end(response.toBuffer());
                            });
                        } else {
                            context.response().end(response.toBuffer());

                        }
                    }
                } else {
                    response.put(Constants.MESSAGE, "Game " + gameID + " not found");
                    context.response().setStatusCode(404).end(response.toBuffer());
                }

            }
        } catch (IllegalArgumentException e) {
            response.put(Constants.MESSAGE, "Error playing " + cardValue + ", " + cardSuit);
            context.response().setStatusCode(417).end(response.toBuffer());
        }

    }

    @Operation(summary = "Choose the trump", method = Constants.CHOOSE_TRUMP_METHOD, operationId = Constants.CHOOSE_TRUMP, tags = {
            Constants.ROUND_TAG}, requestBody = @RequestBody(description = "trump and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = ChooseTrumpBody.class, example = "{\n"
            +
            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
            "  \"" + Constants.CARD_SUIT + "\": \"COINS\"\n" +
            "}"))), responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = ChooseTrumpBody.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "417", description = "Invalid suit."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void chooseTrump(RoutingContext context) {
        String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String cardSuit = context.body().asJsonObject().getString(Constants.CARD_SUIT);
        JsonObject trumpResponse = this.gameService.chooseTrump(gameID, cardSuit);
        if (!trumpResponse.containsKey(Constants.NOT_FOUND)) {
            context.response().end(trumpResponse.toBuffer());
        } else if (trumpResponse.containsKey(Constants.ILLEGAL_TRUMP)) {
            context.response().setStatusCode(401).end(trumpResponse.toBuffer());
        } else {
            context.response().setStatusCode(404).end(trumpResponse.toBuffer());
        }
    }

    @Operation(summary = "Start a new round in a specific game", method = Constants.START_NEW_ROUND_METHOD, operationId = Constants.START_NEW_ROUND, tags = {
            Constants.ROUND_TAG}, requestBody = @RequestBody(description = "id of the game is required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = StartBody.class, example = "{\n"
            +
            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\"\n" +
            "}"))), responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = StartBody.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void startNewRound(RoutingContext context) {
        String uuidAsString = context.body().asJsonObject().getString(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        if (this.gameService.startNewRound(gameID)) {
            context.response().end("New round started");
        } else {
            context.response().setStatusCode(404).end("Game " + gameID + " not found");
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
            @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = StateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game or trick not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void getState(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        String message = this.gameService.getState(gameID).getString(Constants.MESSAGE);
        if (this.gameService.getGames().get(gameID).canStart()) {
            context.response().end(message);
        } else {
            context.response().setStatusCode(404).end(message);
        }
    }

    @Operation(summary = "Check if the round is ended", method = Constants.END_METHOD, operationId = Constants.END_ROUND, // !
            // operationId
            // must
            // be
            // the
            // same
            // as
            // controller
            tags = {Constants.ROUND_TAG}, parameters = {
            @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = IsRoundEndedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void isRoundEnded(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        JsonObject jsonEnd = this.gameService.isRoundEnded(gameID);
        if (!jsonEnd.containsKey(Constants.NOT_FOUND)) {
            context.response().end(jsonEnd.getString(Constants.MESSAGE));
        } else {
            context.response().setStatusCode(404).end(jsonEnd.getString(Constants.MESSAGE));
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
            @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = IsRoundEndedResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void isGameEnded(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        JsonObject jsonEnd = this.gameService.isGameEnded(gameID);
        if (!jsonEnd.containsKey(Constants.NOT_FOUND)) {
            context.response().end(jsonEnd.toBuffer());
        } else {
            context.response().setStatusCode(404).end(jsonEnd.toBuffer());
        }
    }

    @Operation(summary = "Make a call", method = Constants.MAKE_CALL_METHOD, operationId = Constants.MAKE_CALL, tags = {
            Constants.ROUND_TAG}, requestBody = @RequestBody(description = "call, username and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = MakeCallBody.class, example = "{\n"
            +
            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
            "  \"" + Constants.CALL + "\": \"string\",\n" +
            "  \"" + Constants.USERNAME + "\": \"string\"\n" +
            "}"))), responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = MakeCallBody.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "417", description = "Invalid call."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void makeCall(RoutingContext context) {
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String call = context.body().asJsonObject().getString(Constants.CALL);
        String username = context.body().asJsonObject().getString(Constants.USERNAME);
        JsonObject jsonCall = this.gameService.makeCall(gameID, call, username);
        if (!jsonCall.containsKey(Constants.NOT_FOUND)) {
            if (jsonCall.getBoolean(Constants.MESSAGE)) {
                context.response().end("Call " + call + " setted!");
            }
            context.response().setStatusCode(404).end("Invalid call");
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
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void getGames(RoutingContext context) {
        JsonArray jsonGetGames = this.gameService.getJsonGames();
        if (!jsonGetGames.isEmpty()) {
            context.response().end(jsonGetGames.toBuffer());
        } else {
            jsonGetGames.add(Constants.NOT_FOUND);
            context.response().setStatusCode(404).end(jsonGetGames.toBuffer());
        }
    }

    @Operation(summary = "Get the cards on the hands of a specific player", method = Constants.CARDS_ON_HAND_METHOD, operationId = Constants.CARDS_ON_HAND, // !
            // operationId
            // must
            // be
            // the
            // same
            // as
            // controller
            tags = {Constants.ROUND_TAG}, parameters = {
            @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.PATH, name = Constants.USERNAME, required = true, description = "A username", schema = @Schema(type = "string"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = CanStartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void cardsOnHand(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        UUID username = UUID.fromString(context.pathParam(Constants.USERNAME));
        /*
         * String message =
         * this.gameService.getState(gameID).getString(Constants.MESSAGE);
         * if(!this.gameService.canStart(gameID).containsKey(Constants.NOT_FOUND)){
         * context.response().end(message);
         * }
         * context.response().setStatusCode(404).end(message);
         */
    }

    @Operation(summary = "Get the cards on the table", method = Constants.CARDS_ON_TABLE_METHOD, operationId = Constants.CARDS_ON_TABLE, // !
            // operationId
            // must
            // be
            // the
            // same
            // as
            // controller
            tags = {Constants.ROUND_TAG}, parameters = {
            @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID, required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))
    }, responses = {
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json; charset=utf-8", encoding = @Encoding(contentType = "application/json"), schema = @Schema(name = "game", implementation = CanStartResponse.class))),
            @ApiResponse(responseCode = "404", description = "Game not found."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error.")
    })
    public void cardsOnTable(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        /*
         * String message =
         * this.gameService.getState(gameID).getString(Constants.MESSAGE);
         * if(!this.gameService.canStart(gameID).containsKey(Constants.NOT_FOUND)){
         * context.response().end(message);
         * }
         * context.response().setStatusCode(404).end(message);
         */
    }

    public Map<UUID, GameVerticle> getGames() {
        return games;
    }
}
