package chatModule;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

@Api(tags = "Chat Operations", description = "APIs for chat management")
public class ChatController {
	public ChatService service;

	public ChatController(final Vertx vertx) {
		this.service = new ChatService(vertx);
	}

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
		final String msg = context.body().asJsonObject().getString("message");
		this.service.messageReceived(msg);
		context.response().setStatusCode(201).end(); // TODO temp
	}
}
