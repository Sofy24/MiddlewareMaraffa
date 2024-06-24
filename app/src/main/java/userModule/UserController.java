package userModule;

import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.annotations.Api;
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
import userModule.schema.UserLoginSchema;
import userModule.schema.UserRegisterSchema;

// @Api(tags = "login", description = "APIs for user management")
@Api(tags = "User Operations", description = "APIs for user management")
public class UserController {
	private final UserService userService;
	final static Dotenv dotenv = Dotenv.configure().load();
	private static int port = Integer.parseInt(dotenv.get("USER_PORT", "3001"));
	private static String host = dotenv.get("USER_HOST", "localhost");

	public UserController(final Vertx vertx) {
		this.userService = new UserService(vertx, host, port);
	}

	@Operation(summary = "Single user info", description = "Get User info", method = "GET", operationId = "user/:nickname", tags = {
			"User Operations" }, parameters = {
					@Parameter(in = ParameterIn.PATH, name = "nickname", required = true, description = "The unique nickname of the user", schema = @Schema(type = "string")) }, responses = {
							@ApiResponse(responseCode = "200", description = "Search successfull"),
							@ApiResponse(responseCode = "404", description = "User Not Found"),
							@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public void fetchUserInfo(final RoutingContext context) {
		this.userService.getUserInfo(context.pathParam("nickname")).whenComplete((response, error) -> {
			if (error != null) {
				context.response().setStatusCode(500).end(error.getMessage());
			} else {
				switch (response.getString("status")) {
					case "404":
						context.response().setStatusCode(404)
								.end(new JsonObject()
										.put("error", response.getString("error"))
										.toBuffer());
						break;
					case "401":
						context.response().setStatusCode(401)
								.end(new JsonObject()
										.put("error", response.getString("error"))
										.toBuffer());
						break;
					default:
						context.response().setStatusCode(201).end(response.encode());
						break;
				}
			}
		});
	}

	@Operation(summary = "Login operation", description = "Authenticate user and generate token", method = "POST", operationId = "login", tags = {
			"User Operations" }, requestBody = @RequestBody(description = "User's credentials", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = UserLoginSchema.class, example = "{\n"
					+ "\"nickname" + "\": \"user\",\n" + "  \"" + "password" + "\": \"password\"\n"
					+ "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "Login successful"),
							@ApiResponse(responseCode = "404", description = "User Not Found"),
							@ApiResponse(responseCode = "401", description = "Wrong password"),
							@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public void loginRoute(final RoutingContext context) {
		this.userService.loginUser(context.body().asJsonObject().getString("nickname"),
				context.body().asJsonObject().getString("password")).whenComplete((response, error) -> {
					if (error != null) {
						context.response().setStatusCode(500).end(error.getMessage());
					} else {
						switch (response.getString("status")) {
							case "404":
								context.response().setStatusCode(404)
										.end(new JsonObject()
												.put("error", response.getString("error"))
												.toBuffer());
								break;
							case "401":
								context.response().setStatusCode(401)
										.end(new JsonObject()
												.put("error", response.getString("error"))
												.toBuffer());
								break;
							default:
								context.response().setStatusCode(201).end(response.encode());
								break;
						}
					}
				});
	}

	@Operation(summary = "Reset password operation", description = "Reset the password", method = "POST", operationId = "reset", tags = {
			"User Operations" }, requestBody = @RequestBody(description = "User's credentials", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = UserLoginSchema.class, example = "{\n"
					+ "\"nickname" + "\": \"user\",\n" + "  \"" + "password" + "\": \"password\"\n"
					+ "}"))), responses = {
							@ApiResponse(responseCode = "200", description = "Login successful"),
							@ApiResponse(responseCode = "401", description = "Unauthorized"),
							@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public void resetRoute(final RoutingContext context) {
		context.response().setStatusCode(500)
				.end(new JsonObject().put("message", "To be impplemenented !").toBuffer());
	}

	@Operation(summary = "Register operation", description = "Create a new user account", method = "POST", operationId = "register", tags = {
			"User Operations" }, requestBody = @RequestBody(description = "User's details", content = @Content(mediaType = "application/json", encoding = @Encoding(contentType = "application/json"), schema = @Schema(implementation = UserRegisterSchema.class, example = "{\n"
					+ "\"nickname" + "\": \"user\",\n" + "  \"" + "password" + "\": \"password\",\n" + "  \""
					+ "email" + "\": \"mmm@gmail.com\"\n" + "}"))), responses = {
							@ApiResponse(responseCode = "201", description = "User registered successfully"),
							@ApiResponse(responseCode = "400", description = "Bad Request"),
							@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public void registerRoute(final RoutingContext context) {
		this.userService.registerUser(context.body().asJsonObject().getString("nickname"),
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
			"User Operations" }, responses = { @ApiResponse(responseCode = "200", description = "Logout successful"),
					@ApiResponse(responseCode = "401", description = "Unauthorized"),
					@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	public void logoutRoute(final RoutingContext context) {
		// TODO: Implement logout logic
		context.response().setStatusCode(500)
				.end(new JsonObject().put("message", "To be impplemenented !").toBuffer());
	}
}
