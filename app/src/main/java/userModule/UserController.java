package userModule;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import userModule.schema.UserSchema;

// @Api(tags = "login", description = "APIs for user management")
@Api(tags = "User Operations", description = "APIs for user management")
public class UserController {
	private final UserService userService;

	public UserController(final Vertx vertx) {
		this.userService = new UserService(vertx);
	}

	@Operation(summary = "Login operation", description = "Authenticate user and generate token", method = "POST", operationId = "login", tags = {
			"User Operations"}, requestBody = @RequestBody(description = "User's credentials"), responses = {
					@ApiResponse(responseCode = "200", description = "Login successful"),
					@ApiResponse(responseCode = "401", description = "Unauthorized"),
					@ApiResponse(responseCode = "500", description = "Internal Server Error")})
	public void loginRoute(final RoutingContext context) {
		userService.loginUser(context.body().asJsonObject().getString("nickname"),
				context.body().asJsonObject().getString("password")).whenComplete((response, error) -> {
					if (error != null) {
						context.response().setStatusCode(500).end(error.getMessage());
					} else {
						context.response().setStatusCode(201).end(response.encode());
					}
				});
	}

	// TODO non funziona lo schema d'esempio !
	@Operation(summary = "Register operation", description = "Create a new user account", method = "POST", operationId = "register", tags = {
			"User Operations"}, requestBody = @RequestBody(description = "User's details", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = UserSchema.class, example = "{\n"
					+ "  \"" + "nickname" + "\": user,\n" + "  \"" + "password" + "\": \"password\",\n" + "  \""
					+ "email" + "\": mmm@gmail.com,\n" + "}"))), responses = {
							@ApiResponse(responseCode = "201", description = "User registered successfully"),
							@ApiResponse(responseCode = "400", description = "Bad Request"),
							@ApiResponse(responseCode = "500", description = "Internal Server Error")})
	public void registerRoute(final RoutingContext context) {
		final JsonObject jj = new JsonObject();
		jj.put("nickname", "user");
		jj.put("password", "pwd");
		jj.put("email", "mmm@gmail.com,");

		// userService.askService(jj, HttpMethod.POST, "/user").whenComplete((response,
		// error) -> {
		userService.registerUser(context.body().asJsonObject().getString("nickname"),
				context.body().asJsonObject().getString("password"), context.body().asJsonObject().getString("email"))
				.whenComplete((response, error) -> {
					if (error != null) {
						context.response().setStatusCode(500)
								.end(new JsonObject().put("message", error.getMessage()).toBuffer());
					} else {
						context.response().setStatusCode(201).end(response.encode());
					}
				});
	}

	@Operation(summary = "Logout operation", description = "Invalidate user session", method = "POST", operationId = "logout", tags = {
			"User Operations"}, responses = {@ApiResponse(responseCode = "200", description = "Logout successful"),
					@ApiResponse(responseCode = "401", description = "Unauthorized"),
					@ApiResponse(responseCode = "500", description = "Internal Server Error")})
	public void logoutRoute(final RoutingContext context) {
		// TODO: Implement logout logic
	}
}
