package org.example.service;


import io.swagger.models.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.models.media.MediaType;
import io.vertx.ext.web.RoutingContext;
import org.example.httpRest.RouteResponseValue;
import org.example.httpRest.requestBody.CreateGameBody;


public class GameService {
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
                            schema = @Schema(implementation = CreateGameBody.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = "application/json",
                                    encoding = @Encoding(contentType = "application/json"),
                                    schema = @Schema(name = "gamee",
                                            implementation = CreateGameBody.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "game not found."),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error.")
            }
    )
    public String createGame(RoutingContext context) {
        System.out.println("username = " );
        System.out.println("EntityService.createGame" +
                "() " + context.getBody());
       return "Bellaaaaaa";
    }

    public void joinChat(String gameID, String player) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'joinChat'");
    }
}
