package org.example.httpRest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.example.service.GameService;

public class Controller implements IController {
    private final GameService entityService;
    private final List<IRouteResponse> routes = new ArrayList<>();

    public List<IRouteResponse> getRoutes() {
        return routes;
    }

    // public EntityService getEntityService() {
    //     return entityService;
    // }

    public Controller(GameService entityService) {
        this.entityService = entityService;
        this.createGame();
    }

    public void addRoute(IRouteResponse routeResponse){
        if (!this.routes.contains(routeResponse)){
            this.routes.add(routeResponse);
        }
    }

    private void setupController(){

    }

    // @Override
    public void createGame() {
        routes.add(
                new RouteResponse(HttpMethod.POST, "/game/create", entityService::createGame)
                /*new RouteResponse(HttpMethod.POST, "/url", r -> {
                    System.out.println("Hello() " );
                })*/
        );
    }

}
