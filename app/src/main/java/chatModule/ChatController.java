package chatModule;

import java.util.Optional;
import java.util.UUID;

import game.service.GameServiceDecorator;
import game.utils.Constants;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import server.WebSocketVertx;

/**
 * TODO javadoc
 */
@Api(tags = "Chat Operations", description = "APIs for chat management")
public class ChatController {
	private final ChatService service;

	public ChatController(final Vertx vertx, final WebSocketVertx webSocketVertx, final GameServiceDecorator gameServiceDecorator) {
		this.service = new ChatService(vertx, webSocketVertx, gameServiceDecorator.getGames());

	}

	/**
	 * @param context
	 */
	@Operation(summary = "MSG rec operation", description = "Authenticate user and generate token", method = "POST", operationId = "chat", tags = {
			"Chat Operations" }
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
}
