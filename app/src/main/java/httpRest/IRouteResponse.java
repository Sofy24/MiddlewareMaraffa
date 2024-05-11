package httpRest;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public interface IRouteResponse {
    HttpMethod getMethod();

    String getRoute();

    Handler<RoutingContext> getHandler();
}
