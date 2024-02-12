package org.example.httpRest;

import io.vertx.core.http.HttpMethod;

public enum RouteResponseValue {
    CREATEGAME(HttpMethod.POST, "game/create", "createGame"),
    JOINGAME(HttpMethod.POST, "game/join", "joinGame");
    private final HttpMethod method;
    private final String route;
    private final String serviceName;

    RouteResponseValue( HttpMethod method, String route, String serviceName) {
        this.serviceName = serviceName;
        this.route = route;
        this.method = method;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getRoute() {
        return route;
    }

    public HttpMethod getMethod() {
        return method;
    }
}
