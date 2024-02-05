package org.example.experimentsGetPost;

/*import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class UserAPI extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) throws Exception {
        super.start(startFuture);

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // Enable parsing of request bodies
        router.route().handler(BodyHandler.create());

        // GET request for user data
        router.route(HttpMethod.GET, "/users/:id").handler(this::getUser);

        // POST request to create a new user
        //router.route(HttpMethod.POST, "/users").handler(this::createUser);

        server.requestHandler(router).listen(8080);
    }

    private void getUser(RoutingContext routingContext) {
        String userId = routingContext.request().getParam("id");
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");
        response.end("User ID: " + userId);
    }

    /*private void createUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");
        response.end("User created successfully!");
    }
}*/
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;

public class UserVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        HttpServer server = vertx.createHttpServer();


        server.requestHandler(this::handleRequest);

        server.listen(8080, result -> {
            if (result.succeeded()) {
                startPromise.complete();
                System.out.println("HTTP server started on port 8080");
            } else {
                startPromise.fail(result.cause());
            }
        });
    }

    private void handleRequest(HttpServerRequest request) {
        if ("/hello".equals(request.path())) {
            // Respond with a simple message for requests to "/hello"
            request.response().end("Hello, Vert.x!");
        } else if ("/hi".equals(request.path())) {
            System.out.println("post post");
            // Handle POST requests to "/hello" here
            // For example, you can read the request body
            request.bodyHandler(buffer -> {
                String requestBody = buffer.toString();
                // Process the request body as needed

                // Respond with a confirmation message
                request.response().end("POST request received: " + requestBody);
            });
        }
        else {
            // Respond with a 404 Not Found for other paths
            request.response().setStatusCode(404).end();
        }
    }

    private void handlePost(HttpServerRequest request) {
        if ("/hi".equals(request.path()) && request.method().equals("POST")) {
            System.out.println("post post");
            // Handle POST requests to "/hello" here
            // For example, you can read the request body
            request.bodyHandler(buffer -> {
                String requestBody = buffer.toString();
                // Process the request body as needed

                // Respond with a confirmation message
                request.response().end("POST request received: " + requestBody);
            });
        } else {
            // Respond with a 404 Not Found for other paths
            request.response().setStatusCode(404).end();
        }
    }
}
