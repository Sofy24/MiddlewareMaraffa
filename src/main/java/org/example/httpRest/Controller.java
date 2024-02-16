package org.example.httpRest;

import java.util.ArrayList;
import java.util.List;
import io.vertx.core.http.HttpMethod;

import org.example.service.GameServiceDecorator;
import org.example.utils.Constants;

public class Controller implements IController {
    private final GameServiceDecorator entityService;
    private final List<IRouteResponse> routes = new ArrayList<>();

    public Controller(GameServiceDecorator entityService) {
        this.entityService = entityService;
        this.addRoutes();
    }

    /**Add all Maraffa's routes*/
    private void addRoutes() {
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.CREATE_GAME , entityService::createGame));
        routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.JOIN_GAME, entityService::joinGame));
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.PLAY_CARD, entityService::playCard));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.CAN_START, entityService::canStart));
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.CHOOSE_TRUMP , entityService::chooseTrump));
        routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.START_NEW_ROUND, entityService::startNewRound));
    }

    public List<IRouteResponse> getRoutes() {
        return routes;
    }
}
