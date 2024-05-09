package httpRest;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.http.HttpMethod;

import game.service.GameServiceDecorator;
import game.utils.Constants;

public class Controller implements IController {
    private final GameServiceDecorator entityService;
    private final List<IRouteResponse> routes = new ArrayList<>();

    public Controller(GameServiceDecorator entityService) {
        this.entityService = entityService;
        this.addRoutes();
    }

    /**
     * Add all Maraffa's routes
     */
    private void addRoutes() {
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.CREATE_GAME, entityService::createGame));
        routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.JOIN_GAME, entityService::joinGame));
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.PLAY_CARD, entityService::playCard));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.CAN_START, entityService::canStart));
        routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.START_GAME, entityService::startGame));
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.CHOOSE_TRUMP, entityService::chooseTrump));
        routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.START_NEW_ROUND, entityService::startNewRound));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.STATE, entityService::getState));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.END_ROUND, entityService::isRoundEnded));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.END_GAME, entityService::isGameEnded));
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.MAKE_CALL, entityService::makeCall));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.GAMES, entityService::getGames));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.COINS_4, entityService::coins4));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.CARDS_ON_HAND, entityService::cardsOnHand));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.CARDS_ON_TABLE, entityService::cardsOnTable));
//TODO delete game
    }

    public List<IRouteResponse> getRoutes() {
        return routes;
    }
}
