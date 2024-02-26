package org.example.service;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.example.game.Card;
import org.example.game.CardSuit;
import org.example.game.CardValue;
import org.example.game.GameVerticle;
import org.example.repository.AbstractStatisticManager;
import org.example.service.schema.*;
import org.example.utils.Constants;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameServiceDecorator {
    private final Map<UUID, GameVerticle> games = new ConcurrentHashMap<>();
    private final GameService gameService;
    public GameServiceDecorator(Vertx vertx) {
        this.gameService = new GameService(vertx);
    }

    public GameServiceDecorator(Vertx vertx, AbstractStatisticManager statisticManager) {
        this.gameService = new GameService(vertx, statisticManager);
    }

    @Operation(summary = "Create new game", method = Constants.CREATE_GAME_METHOD, operationId = Constants.CREATE_GAME,
            tags = { Constants.GAME_TAG },
            requestBody = @RequestBody(
                    description = "insert username and the number of players",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            encoding = @Encoding(contentType = "application/json"),
                            schema = @Schema(implementation = CreateGameBody.class, example =
                                    "{\n" +
                                            "  \"" + Constants.NUMBER_OF_PLAYERS + "\": 4,\n" +
                                            "  \"" + Constants.USERNAME + "\": \"string\",\n" +
                                            "  \"" + Constants.EXPECTED_SCORE + "\": 41\n" +
                                            "}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game-creation",
                                            implementation = CreateGameBody.class)
                            )
                    ),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void createGame(RoutingContext context) {
        Integer numberOfPlayers = (Integer) context.body().asJsonObject().getValue(Constants.NUMBER_OF_PLAYERS);
        String username = String.valueOf(context.body().asJsonObject().getValue(Constants.USERNAME));
        Integer expectedScore = (Integer) context.body().asJsonObject().getValue(Constants.EXPECTED_SCORE);
        JsonObject jsonGame = this.gameService.createGame(numberOfPlayers, username, expectedScore);
        context.response().end(jsonGame.toString());
    }

    @Operation(summary = "Join a specific game", method = Constants.JOIN_GAME_METHOD, operationId = Constants.JOIN_GAME,
            tags = { Constants.GAME_TAG },
            requestBody = @RequestBody(
                    description = "username and id of the game are required",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            encoding = @Encoding(contentType = "application/json"),
                            schema = @Schema(implementation = JoinGameBody.class, example =
                                    "{\n" +
                                            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                                            "  \"" + Constants.USERNAME + "\": \"string\"\n" +
                                            "}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = JoinGameBody.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "401", description = "Reached the limit of maximum players in the game."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void joinGame(RoutingContext context) {
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String username = String.valueOf(context.body().asJsonObject().getValue(Constants.USERNAME));
        JsonObject joinResponse = this.gameService.joinGame(gameID, username);
        if(joinResponse.containsKey(Constants.NOT_FOUND) ){
            context.response().setStatusCode(404).end(joinResponse.getString(Constants.MESSAGE));
        } else if (joinResponse.containsKey(Constants.FULL) ) {
            context.response().setStatusCode(401).end(joinResponse.getString(Constants.MESSAGE));
        }
        context.response().end(joinResponse.getString(Constants.MESSAGE));
    }

    @Operation(summary = "Check if a game can start", method = Constants.CAN_START_METHOD, operationId = Constants.CAN_START, //! operationId must be the same as controller
            tags = { Constants.GAME_TAG },
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID,
                            required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string") )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json; charset=utf-8",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = CanStartResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void canStart(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        JsonObject startResponse = this.gameService.canStart(gameID);
        String message = startResponse.getString(Constants.MESSAGE);
        if(!startResponse.containsKey(Constants.NOT_FOUND)){
            context.response().end(message);
        }
        context.response().setStatusCode(404).end(message);
    }

    @Operation(summary = "A player plays a card in a specific game", method = Constants.PLAY_CARD_METHOD, operationId = Constants.PLAY_CARD,
            tags = { Constants.ROUND_TAG },
            requestBody = @RequestBody(
                    description = "username, card and id of the game are required",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            encoding = @Encoding(contentType = "application/json"),
                            schema = @Schema(implementation = PlayCardBody.class, example =
                                    "{\n" +
                                            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                                            "  \"" + Constants.USERNAME + "\": \"string\",\n" +
                                            "  \"" + Constants.CARD_VALUE + "\": 0,\n" +
                                            "  \"" + Constants.CARD_SUIT + "\": \"string\"\n" +
                                            "}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = PlayCardBody.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game or username not found."),
                    @ApiResponse(responseCode = "401", description = "Invalid card."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void playCard(RoutingContext context){
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        Integer cardValue = (Integer) context.body().asJsonObject().getValue(Constants.CARD_VALUE);
        String cardSuit = String.valueOf(context.body().asJsonObject().getValue(Constants.CARD_SUIT));
        Card<CardValue, CardSuit> card = new Card<>(CardValue.fromInteger(cardValue), CardSuit.fromUppercaseString(cardSuit.toUpperCase()));
        String username = String.valueOf(context.body().asJsonObject().getValue(Constants.USERNAME));
        if(card.cardSuit().equals(CardSuit.NONE) || card.cardValue().equals(CardValue.NONE)){
            context.response().setStatusCode(401).end("Invalid " + card);
        } else {
            if (this.gameService.playCard(gameID, username, card)) {
                context.response().end(card + " played by " + username);
            } else {
                context.response().setStatusCode(404).end("Game " + gameID + " not found");
            }
        }
    }

    @Operation(summary = "Choose the trump", method = Constants.CHOOSE_TRUMP_METHOD, operationId = Constants.CHOOSE_TRUMP,
            tags = { Constants.ROUND_TAG },
            requestBody = @RequestBody(
                    description = "trump and id of the game are required",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            encoding = @Encoding(contentType = "application/json"),
                            schema = @Schema(implementation = ChooseTrumpBody.class, example =
                                    "{\n" +
                                            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                                            "  \"" + Constants.CARD_SUIT + "\": \"string\"\n" +
                                            "}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = ChooseTrumpBody.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "401", description = "Invalid suit."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void chooseTrump(RoutingContext context) {
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String cardSuit = String.valueOf(context.body().asJsonObject().getValue(Constants.CARD_SUIT));
        JsonObject trumpResponse = this.gameService.chooseTrump(gameID, cardSuit);
        if(!trumpResponse.containsKey(Constants.NOT_FOUND)){
            context.response().end(trumpResponse.getString(Constants.MESSAGE));
        } else if (trumpResponse.containsKey(Constants.ILLEGAL_TRUMP)){
            context.response().setStatusCode(401).end(trumpResponse.getString(Constants.MESSAGE));
        } else {
            context.response().setStatusCode(404).end(trumpResponse.getString(Constants.MESSAGE));
        }
    }

    @Operation(summary = "Start a new round in a specific game", method = Constants.START_NEW_ROUND_METHOD, operationId = Constants.START_NEW_ROUND,
            tags = { Constants.ROUND_TAG },
            requestBody = @RequestBody(
                    description = "id of the game is required",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            encoding = @Encoding(contentType = "application/json"),
                            schema = @Schema(implementation = StartNewRoundBody.class, example =
                                    "{\n" +
                                            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\"\n" +
                                            "}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = StartNewRoundBody.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void startNewRound(RoutingContext context) {
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        if(this.gameService.startNewRound(gameID)){
            context.response().end("New round started");
        } else {
            context.response().setStatusCode(404).end("Game " + gameID + " not found");
        }
    }

    @Operation(summary = "Get the state of a specific game", method = Constants.STATE_METHOD, operationId = Constants.STATE, //! operationId must be the same as controller
            tags = { Constants.GAME_TAG },
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID,
                            required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string") )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json; charset=utf-8",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = StateResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game or trick not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void getState(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        String message = this.gameService.getState(gameID).getString(Constants.MESSAGE);
        if(this.gameService.getGames().get(gameID).canStart()){
            context.response().end(message);
        } else {
            context.response().setStatusCode(404).end(message);
        }
    }

    @Operation(summary = "Check if the round is ended", method = Constants.END_METHOD, operationId = Constants.END_ROUND, //! operationId must be the same as controller
            tags = { Constants.ROUND_TAG },
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID,
                            required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string") )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json; charset=utf-8",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = isRoundEnded.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void isRoundEnded(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        JsonObject jsonEnd = this.gameService.isRoundEnded(gameID);
        if(!jsonEnd.containsKey(Constants.NOT_FOUND)){
            context.response().end(jsonEnd.getString(Constants.MESSAGE));
        }
        context.response().setStatusCode(404).end(jsonEnd.getString(Constants.MESSAGE));
    }

    @Operation(summary = "Check if the game is ended", method = Constants.END_METHOD, operationId = Constants.END_GAME, //! operationId must be the same as controller
            tags = { Constants.GAME_TAG },
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID,
                            required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string") )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json; charset=utf-8",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = isRoundEnded.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void isGameEnded(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        JsonObject jsonEnd = this.gameService.isGameEnded(gameID);
        if(!jsonEnd.containsKey(Constants.NOT_FOUND)){
            context.response().end(jsonEnd.getString(Constants.MESSAGE));
        }
        context.response().setStatusCode(404).end(jsonEnd.getString(Constants.MESSAGE));
    }

    @Operation(summary = "Make a call", method = Constants.MAKE_CALL_METHOD, operationId = Constants.MAKE_CALL,
            tags = { Constants.ROUND_TAG },
            requestBody = @RequestBody(
                    description = "call, username and id of the game are required",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            encoding = @Encoding(contentType = "application/json"),
                            schema = @Schema(implementation = MakeCallBody.class, example =
                                    "{\n" +
                                            "  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                                            "  \"" + Constants.CALL + "\": \"string\",\n" +
                                            "  \"" + Constants.USERNAME + "\": \"string\"\n" +
                                            "}")
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = MakeCallBody.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "401", description = "Invalid call."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void makeCall(RoutingContext context) {
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String call = String.valueOf(context.body().asJsonObject().getValue(Constants.CALL));
        String username = String.valueOf(context.body().asJsonObject().getValue(Constants.USERNAME));
        JsonObject jsonCall = this.gameService.makeCall(gameID, call, username);
        if(!jsonCall.containsKey(Constants.NOT_FOUND)){
            if(jsonCall.getBoolean(Constants.MESSAGE)){
                context.response().end("Call " + call + " setted!");
            }
            context.response().setStatusCode(404).end("Invalid call");
        } else {
            context.response().setStatusCode(404).end(jsonCall.getString(Constants.MESSAGE));
        }
    }

    @Operation(summary = "Get the cards on the hands of a specific player", method = Constants.CARDS_ON_HAND_METHOD, operationId = Constants.CARDS_ON_HAND, //! operationId must be the same as controller
            tags = { Constants.ROUND_TAG },
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID,
                            required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string") ),
                    @Parameter(in = ParameterIn.PATH, name = Constants.USERNAME,
                            required = true, description = "A username", schema = @Schema(type = "string") )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json; charset=utf-8",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = CanStartResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void cardsOnHand(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        UUID username = UUID.fromString(context.pathParam(Constants.USERNAME));
        /*String message = this.gameService.getState(gameID).getString(Constants.MESSAGE);
        if(!this.gameService.canStart(gameID).containsKey(Constants.NOT_FOUND)){
            context.response().end(message);
        }
        context.response().setStatusCode(404).end(message);*/
    }

    @Operation(summary = "Get the cards on the table", method = Constants.CARDS_ON_TABLE_METHOD, operationId = Constants.CARDS_ON_TABLE, //! operationId must be the same as controller
            tags = { Constants.ROUND_TAG },
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID,
                            required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string") )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json; charset=utf-8",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "game",
                                            implementation = CanStartResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void cardsOnTable(RoutingContext context) {
        UUID gameID = UUID.fromString(context.pathParam(Constants.GAME_ID));
        /*String message = this.gameService.getState(gameID).getString(Constants.MESSAGE);
        if(!this.gameService.canStart(gameID).containsKey(Constants.NOT_FOUND)){
            context.response().end(message);
        }
        context.response().setStatusCode(404).end(message);*/
    }

    public Map<UUID, GameVerticle> getGames() {
        return games;
    }
}
