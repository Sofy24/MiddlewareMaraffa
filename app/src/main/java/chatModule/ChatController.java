package chatModule;

import java.util.Optional;
import java.util.UUID;

import chatModule.schema.ChatMessageBody;
import chatModule.schema.NotificationBody;
import game.service.GameServiceDecorator;
import game.utils.Constants;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import server.WebSocketVertx;

/**
 * TODO javadoc
 */
@Api(tags = "Chat Operations", description = "APIs for chat management")
public class ChatController {
	private final ChatService service;

	public ChatController(final Vertx vertx, final WebSocketVertx webSocketVertx,
			final GameServiceDecorator gameServiceDecorator) {
		this.service = new ChatService(vertx, webSocketVertx, gameServiceDecorator.getGames());

	}

	/**
	 * @param context
	 */
	@Operation(summary = "MSG rec operation", description = "Authenticate user and generate token", method = "POST", operationId = "chat", tags = {
			"Chat Operations" }, requestBody = @RequestBody(description = "username, trump and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = ChatMessageBody.class, example = "{\n"
					+
					"  \"" + Constants.AUTHOR + "\": \"mega\",\n" +
					"  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
					"  \"" + Constants.MESSAGE + "\": \"Ciao a tutti !\"" +
					"}")))
	// requestBody = @RequestBody(description = "User's credentials"), responses =
	// {
	// @ApiResponse(responseCode = "200", description = "Login successful"),
	// @ApiResponse(responseCode = "401", description = "Unauthorized"),
	// @ApiResponse(responseCode = "500", description = "Internal Server Error")
	// }
	)
	public void messageReceived(final RoutingContext context) {
		final Optional<UUID> gameID = context.pathParam(Constants.GAME_ID) != null
				? Optional.of(UUID.fromString(context.pathParam(Constants.GAME_ID)))
				: Optional.empty();
		final String author = context.body().asJsonObject().getString("author");
		final String msg = context.body().asJsonObject().getString("message");
		this.service.messageReceived(msg, gameID, author);
		context.response().setStatusCode(java.net.HttpURLConnection.HTTP_ACCEPTED).end(); // TODO temp
	}

	/**
	 * @param context
	 */
	@Operation(summary = "notification operation", description = "Sends a notification to all the members of a team", method = "POST", operationId = "notification", tags = {
			"Chat Operations" }, requestBody = @RequestBody(description = "username, trump and id of the game are required", required = true, content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = NotificationBody.class, example = "{\n"
					+
					"  \"" + Constants.GAME_ID + "\": \"123e4567-e89b-12d3-a456-426614174000\",\n" +
					"  \"" + Constants.MESSAGE + "\": \"Ciao a tutti !\"" +
					"}")))

	// requestBody = @RequestBody(description = "User's credentials"), responses =
	// {
	// @ApiResponse(responseCode = "200", description = "Login successful"),
	// @ApiResponse(responseCode = "401", description = "Unauthorized"),
	// @ApiResponse(responseCode = "500", description = "Internal Server Error")
	// }
	)
	public void notificationReceived(final RoutingContext context) {
		final Optional<UUID> gameID = context.pathParam(Constants.GAME_ID) != null
				? Optional.of(UUID.fromString(context.pathParam(Constants.GAME_ID)))
				: Optional.empty();
		final String msg = context.body().asJsonObject().getString("message");
		this.service.notificationReceived(msg, gameID);
		context.response().setStatusCode(java.net.HttpURLConnection.HTTP_ACCEPTED).end(); // TODO temp
	}
}
