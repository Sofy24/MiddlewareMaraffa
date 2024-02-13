package org.example.service;



import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import org.example.game.GameVerticle;
import org.example.httpRest.RouteResponseValue;
import org.example.repository.AbstractStatisticManager;
import org.example.service.requestBody.CreateGameBody;
import org.example.service.requestBody.JoinGameBody;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public class GameService {
    private final Map<UUID, GameVerticle> games = new ConcurrentHashMap<>();
    private final Vertx vertx;
    private AbstractStatisticManager statisticManager; //TODO inizializzalo


    public GameService(Vertx vertx) {
        this.vertx = vertx;
    }

    @Operation(summary = "Create new game", method = "POST", operationId = "game/create", //! operationId must be the same as controller
            tags = {
                    "Game"
            },
            /*parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "gameID",
                            required = true, description = "The unique ID belonging to the game", schema = @Schema(type = "string"))
            },*/
            requestBody = @RequestBody(
                    description = "username and the number of players are required",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            encoding = @Encoding(contentType = "application/json"),
                            schema = @Schema(implementation = CreateGameBody.class, example =
                                    "{\n" +
                                            "  \"username\": \"string\",\n" +
                                            "  \"numberOfPlayers\": 0\n" +
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
                    @ApiResponse(responseCode = "404", description = "Game not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public void createGame(RoutingContext context) {
        Integer numberOfPlayers = (Integer) context.body().asJsonObject().getValue("numberOfPlayers");
        String username = String.valueOf(context.body().asJsonObject().getValue("username"));
        JsonObject jsonGame = new JsonObject();
        UUID newId = UUID.randomUUID();
        GameVerticle currentGame = new GameVerticle(newId, username, numberOfPlayers, this.statisticManager);
        this.games.put(newId, currentGame);
        vertx.deployVerticle(currentGame);
        jsonGame.addProperty("id", String.valueOf(newId));
        context.response().end(jsonGame.toString());
    }

    @Operation(summary = "Join a specific game", method = "POST", operationId = "game/join",
            tags = {
                    "Game"
            },
            requestBody = @RequestBody(
                    description = "username and id of the game are required",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            encoding = @Encoding(contentType = "application/json"),
                            schema = @Schema(implementation = JoinGameBody.class, example =
                                    "{\n" +
                                            "  \"gameID\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
                                            "  \"username\": \"string\"\n" +
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
        String uuidAsString = (String) context.body().asJsonObject().getValue("gameID");
        UUID gameID = UUID.fromString(uuidAsString);
        String username = String.valueOf(context.body().asJsonObject().getValue("username"));
        if(this.games.get(gameID) != null){
            this.games.get(gameID).addUser(username);
            context.response().end("Game "+ gameID +" joined by " + username);
        }
        context.response().setStatusCode(404).end("Game "+ gameID +" joined by " + username);
    }
}
