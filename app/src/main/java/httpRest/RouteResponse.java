package httpRest;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class RouteResponse implements IRouteResponse {
	HttpMethod method;
	String route;
	Handler<RoutingContext> handler;

	public RouteResponse(final HttpMethod method, final String route, final Handler<RoutingContext> handler) {
		this.method = method;
		this.route = route;
		this.handler = handler;
	}

	@Override
	public HttpMethod getMethod() {
		return this.method;
	}

	@Override
	public String getRoute() {
		return this.route;
	}

	@Override
	public Handler<RoutingContext> getHandler() {
		return this.handler;
	}
}
