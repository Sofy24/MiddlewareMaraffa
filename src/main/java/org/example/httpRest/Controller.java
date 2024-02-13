package org.example.httpRest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.example.service.GameService;
import org.example.utils.Constants;

public class Controller implements IController {
    private final GameService entityService;
    private final List<IRouteResponse> routes = new ArrayList<>();

    public List<IRouteResponse> getRoutes() {
        return routes;
    }

    public Controller(GameService entityService) {
        this.entityService = entityService;
        this.addRoutes();
    }

/*    public void addRoute(IRouteResponse routeResponse){
        if (!this.routes.contains(routeResponse)){
            this.routes.add(routeResponse);
        }
    }*/

    private void setupController(){

    }

    // @Override
    public void addRoutes() {
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.CREATE_GAME , entityService::createGame));
        routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.JOIN_GAME, entityService::joinGame));
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.PLAY_CARD, entityService::playCard));
        routes.add(new RouteResponse(HttpMethod.GET, "/" + Constants.CAN_START, entityService::canStart));
        routes.add(new RouteResponse(HttpMethod.POST, "/" + Constants.CHOOSE_TRUMP , entityService::chooseSuit));
        routes.add(new RouteResponse(HttpMethod.PATCH, "/" + Constants.START_NEW_ROUND, entityService::startNewRound));
    }

}
