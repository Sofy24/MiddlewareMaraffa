package org.example.httpRest;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.example.game.Trick;

public class BusinessLogicController {
    private Vertx vertx;
    private static final int PORT = 3000;
    private static final String LOCALHOST = "localhost";
    private final Router router;
    private WebClient webClient;

    public BusinessLogicController(Vertx vertx) {
        this.vertx = vertx;
        this.router = Router.router(vertx);
        this.webClient = WebClient.create(vertx);
        //setup();
    }

    /*private void setup(){
        router.post("/computeScore").handler(this::computeScore);
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, http -> {
                    if (http.succeeded()) {
                        //startFuture.complete();
                        System.out.println("HTTP server started on port 8080");
                    } else {
                        //startFuture.fail(http.cause());
                        System.out.println("erorr");
                    }
                });
    }*/

    private void computeScore(RoutingContext routingContext, Trick trick, String trump) {
        webClient.post(PORT, LOCALHOST, "/computeScore")
                .sendJsonObject(new JsonObject().put("trick", trick).put("trump", trump),
                        ar -> {
                            if (ar.succeeded()) {
                                HttpResponse<Buffer> response = ar.result();
                                if (response.statusCode() == 201) {
                                    routingContext.response()
                                            .setStatusCode(201)
                                            .end(response.bodyAsString());
                                } else {
                                    routingContext.response()
                                            .setStatusCode(417)
                                            .end("Computation of score failed");
                                }
                            } else {
                                routingContext.fail(ar.cause());
                            }
                        });
    }
}
