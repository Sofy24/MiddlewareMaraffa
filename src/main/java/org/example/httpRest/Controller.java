package org.example.httpRest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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
        routes.add(new RouteResponse(HttpMethod.POST, "/game/create", entityService::createGame));
        routes.add(new RouteResponse(HttpMethod.POST, "/game/join", entityService::joinGame));
                /*new RouteResponse(HttpMethod.POST, "/url", r -> {
                    System.out.println("Hello() " );
                })*/
    }

}
