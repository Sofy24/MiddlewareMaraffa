package httpRest;

import java.util.ArrayList;
import java.util.List;

import chatModule.ChatController;
import game.service.GameServiceDecorator;
import game.utils.Constants;
import io.vertx.core.http.HttpMethod;
import userModule.UserController;

public class Controller implements IController {
	private final GameServiceDecorator entityService;
	private final List<IRouteResponse> routes = new ArrayList<>();
	private final UserController userController;
	private final ChatController chatController;

	public Controller(final GameServiceDecorator entityService, final UserController userController,
			final ChatController chatController) {
		this.entityService = entityService;
		this.userController = userController;
		this.chatController = chatController;
		this.addRoutes();
	}

	/** Add all Maraffa's routes */
	private void addRoutes() {
		this.routes
				.add(new RouteResponse(HttpMethod.POST, "/" + Constants.CREATE_GAME, this.entityService::createGame));
		this.routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.JOIN_GAME, this.entityService::joinGame));
		this.routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.PLAY_CARD, this.entityService::playCard));
		this.routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.CAN_START, this.entityService::canStart));
		this.routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.START_GAME, this.entityService::startGame));
		this.routes
				.add(new RouteResponse(HttpMethod.POST, "/" + Constants.CHOOSE_TRUMP, this.entityService::chooseTrump));
		this.routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.START_NEW_ROUND,
				this.entityService::startNewRound));
		this.routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.CHANGE_TEAM,
				this.entityService::changeTeam));
		this.routes.add(
				new RouteResponse(HttpMethod.GET, "/" + Constants.PLAYER_CARDS, this.entityService::getPlayerCard));
		this.routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.STATE, this.entityService::getState));
		this.routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.END_ROUND, this.entityService::isRoundEnded));
		this.routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.END_GAME, this.entityService::isGameEnded));
		this.routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.MAKE_CALL, this.entityService::makeCall));
		this.routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.GAMES, this.entityService::getGames));
		this.routes
				.add(new RouteResponse(HttpMethod.GET, "/" + Constants.CARDS_ON_HAND, this.entityService::cardsOnHand));
		this.routes.add(
				new RouteResponse(HttpMethod.GET, "/" + Constants.CARDS_ON_TABLE, this.entityService::cardsOnTable));
		// TODO delete game
		// user management
		this.routes.add(new RouteResponse(HttpMethod.GET, "/user/:nickname", this.userController::fetchUserInfo));
		this.routes.add(new RouteResponse(HttpMethod.POST, "/login", this.userController::loginRoute));
		this.routes.add(new RouteResponse(HttpMethod.POST, "/register", this.userController::registerRoute));
		this.routes.add(new RouteResponse(HttpMethod.POST, "/resetPassword", this.userController::registerRoute));
		this.routes.add(new RouteResponse(HttpMethod.POST, "/logout", this.userController::logoutRoute));
		// chat management
		this.routes.add(new RouteResponse(HttpMethod.POST, "/chat", this.chatController::messageReceived));
	}

	public List<IRouteResponse> getRoutes() {
		return this.routes;
	}
}
