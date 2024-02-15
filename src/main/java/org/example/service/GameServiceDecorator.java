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

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
                                            "  \"" + Constants.USERNAME + "\": \"string\"\n" +
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
        JsonObject jsonGame = this.gameService.createGame(numberOfPlayers, username);
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
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void joinGame(RoutingContext context) {
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String username = String.valueOf(context.body().asJsonObject().getValue(Constants.USERNAME));
        if(this.games.get(gameID) != null){
            this.games.get(gameID).addUser(username);
            context.response().end("Game "+ gameID +" joined by " + username);
        }
        context.response().setStatusCode(404).end("Game "+ gameID + " or username " + username + " not found ");
    }

    @Operation(summary = "Check if a game can start", method = Constants.CAN_START_METHOD, operationId = Constants.CAN_START, //! operationId must be the same as controller
            tags = { Constants.GAME_TAG },
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = Constants.GAME_ID,
                            required = false, description = "The unique ID belonging to the game", schema = @Schema(type = "string") )
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
        if(this.games.get(gameID) != null){
            if (this.games.get(gameID).canStart()) {
                context.response().end("The game " + gameID + " can start");
            } else {
                context.response().end("The game " + gameID + " can't start");
            }
        }

        context.response().setStatusCode(404).end("Game "+ gameID +" not found");
    }

    @Operation(summary = "A player plays a card in a specific game", method = Constants.PLAY_CARD_METHOD, operationId = Constants.PLAY_CARD,
            tags = { Constants.GAME_TAG },
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
        if(this.games.get(gameID) != null){
            this.games.get(gameID).addCard(card, username);
            context.response().end(card +" played by " + username);
        }
        context.response().setStatusCode(404).end("Game "+ gameID +" not found");
    }

    @Operation(summary = "Choose the trump", method = Constants.CHOOSE_TRUMP_METHOD, operationId = Constants.CHOOSE_TRUMP,
            tags = { Constants.GAME_TAG },
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
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void chooseSuit(RoutingContext context) {
        String uuidAsString = (String) context.body().asJsonObject().getValue(Constants.GAME_ID);
        UUID gameID = UUID.fromString(uuidAsString);
        String cardSuit = String.valueOf(context.body().asJsonObject().getValue(Constants.CARD_SUIT));
        if(this.games.get(gameID) != null){
            this.games.get(gameID).chooseSuit(CardSuit.fromUppercaseString(cardSuit.toUpperCase()));
            context.response().end( CardSuit.fromUppercaseString(cardSuit.toUpperCase()) + " setted as trump ");
        }
        context.response().setStatusCode(404).end("Game " + gameID + " not found");
    }

    @Operation(summary = "Start a new round in a specific game", method = Constants.START_NEW_ROUND_METHOD, operationId = Constants.START_NEW_ROUND,
            tags = { Constants.GAME_TAG },
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
        if(this.games.get(gameID) != null){
            this.games.get(gameID).startNewRound();
            context.response().end("New round started");
        }
        context.response().setStatusCode(404).end("Game "+ gameID +" not found");
    }

    public Map<UUID, GameVerticle> getGames() {
        return games;
    }
}
