package org.example.httpRest;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class Controller extends AbstractController {
    // private EntityService entityService;
    private List<IRouteResponse> routes = new ArrayList<IRouteResponse>();

    public List<IRouteResponse> getRoutes() {
        return routes;
    }

    // public EntityService getEntityService() {
    //     return entityService;
    // }

    public Controller(/*final EntityService entityService*/) {
        // this.entityService = entityService;
        this.createChat();
    }

    // @Override
    public void createChat() {
        routes.add(
                // new RouteResponse(HttpMethod.POST, "/chat/create/:gameID", entityService::createChat)
                new RouteResponse(HttpMethod.POST, "/url", r -> {
                    System.out.println("Hello() " );
                })
        );
    }

}
