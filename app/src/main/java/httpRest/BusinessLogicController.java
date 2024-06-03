// package httpRest;

// import java.util.List;
// import java.util.concurrent.CompletableFuture;
// import java.util.stream.Collector;
// import java.util.UUID;
// import com.google.common.primitives.Booleans;

// import io.vertx.core.impl.logging.Logger;
// import io.vertx.core.impl.logging.LoggerFactory;
// import server.AppServer;
// import game.Card;
// import game.Trick;
// import game.service.GameService;
// import io.vertx.core.Vertx;
// import io.vertx.core.json.JsonObject;
// import io.vertx.ext.web.Router;
// import io.vertx.ext.web.client.WebClient;
// import io.vertx.ext.web.codec.BodyCodec;

// public class BusinessLogicController {
//     private Vertx vertx;
//     private static final int PORT = 3000;
//     private static final String LOCALHOST = "127.0.0.1";
//     private final Router router;
//     private static final Logger LOGGER = LoggerFactory.getLogger(AppServer.class);
//     private GameService gameService;

//     public BusinessLogicController(Vertx vertx, GameService gameService) {
//         this.vertx = vertx;
//         this.router = Router.router(vertx);
//         this.gameService = gameService;
//     }

//     /**@param userPosition the position of the user who played the card
//      * @param suitFinished if the suit is finished
//      * @param gameID the id of the game
//      * compute the end of a trick
//      */
//     public void isTrickEnded(int userPosition, Boolean suitFinished, UUID gameID) {
//         JsonObject response = new JsonObject();
//         Trick latestTrick = this.gameService.getGames().get(gameID).getLatestTrick();
//         System.out.println("latest =" + latestTrick.toString());
//         this.gameService.getGames().get(gameID).setIsSuitFinished(suitFinished, userPosition);
//         this.vertx.eventBus().consumer("comp-score", message -> {
//         if (latestTrick.isCompleted()) {
//             System.out.println("completed");
//             this.gameService.getGames().get(gameID).incrementCurrentState();
            
//                 LOGGER.info("Received message: " + message.body());
//                 computeScore(latestTrick,
//                 this.gameService.getGames().get(gameID).getTrump().getValue().toString(),
//                 this.gameService.getGames().get(gameID).getGameMode().toString(),
//                 this.gameService.getGames().get(gameID).getIsSuitFinished())
//                 .whenComplete((result, err) -> {
//                 if (err != null) {
//                     LOGGER.error("Error in computing the score");
//                     message.reply("Error in computing the score");
//                 } else{
//                     response.put("result", result);
//                     this.gameService.getGames().get(gameID)
//                         .setTurn(result.getInteger("winningPosition"));
//                     this.gameService.getGames().get(gameID).setScore(result.getInteger("score"),
//                         result.getBoolean("firstTeam"));// TODO i punti della businesslogic sono
//                                                         // moltiplicati per 3
//                     response.put("turn", this.gameService.getGames().get(gameID).getTurn());
//                     System.out
//                         .println("Turn: " + this.gameService.getGames().get(gameID).getTurn());
//                     //context.response().end(response.toBuffer());
//                     message.reply(response);
//                 }
//             });
//         } else {
//         message.reply("Trick not completed yet");
//      }
//     });
// }

//     public CompletableFuture<JsonObject> getShuffledDeck(final Integer numberOfPlayers) {
//         CompletableFuture<JsonObject> future = new CompletableFuture<>();
//         System.out.println("Getting the shuffled deck");
//         WebClient.create(vertx).get(PORT, LOCALHOST, "/games/startRound")
//                 // .ssl(true)
//                 .putHeader("Accept",
//                         "application/json") // (4)
//                 .as(BodyCodec.jsonObject())
//                 .send(handler -> {
//                     System.out.println("Got any response ??");
//                     if (handler.succeeded()) {
//                         future.complete(handler.result().body());
//                     } else {
//                         System.out.println("Error in getting the shuffled deck");
//                         System.out.println(handler.cause().getMessage());
//                         future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
//                     }
//                 });
//         // .onFailure(
//         // fail -> {
//         // System.out.println("Error in getting the shuffled deck");
//         // System.out.println(fail.getMessage());
//         // future.completeExceptionally(fail);
//         // }
//         // )
//         // .onSuccess(response -> {
//         // System.out.println("Got the shuffled deck");
//         // if (response.statusCode() == 200) {
//         // JsonObject json = response.bodyAsJsonObject();
//         // System.out.println(json);
//         // future.complete(json);
//         // } else {
//         // System.out.println("Error in getting the shuffled deck");
//         // }
//         // });
//         return future;
//     }

//     /**
//      * perform a post request to business logic in order to compute the score, get the winning team and position
//      *
//      * @param trick the completed trick with which it computes the score
//      * @param trump used while computing the score
//      * @return a completable future of the json response
//      */
//     public CompletableFuture<JsonObject> computeScore(Trick trick, String trump, String mode, List<Boolean> isSuitFinishedList) {
//         int[] cards = trick.getCards().stream().mapToInt(card -> Integer.parseInt(card)).toArray();
//         boolean[] isSuitFinished = Booleans.toArray(isSuitFinishedList);
//         JsonObject requestBody = new JsonObject()
//                 .put("trick", cards)
//                 .put("trump", Integer.parseInt(trump))
//                 .put("mode", mode)
//                 .put("isSuitFinished", isSuitFinished);
//         CompletableFuture<JsonObject> future = new CompletableFuture<>();
//         LOGGER.info("Computing the score");
//         WebClient.create(vertx).post(PORT, LOCALHOST, "/games/computeScore")
//                 .putHeader("Accept", "application/json")
//                 .as(BodyCodec.jsonObject())
//                 .sendJsonObject(requestBody, handler -> {
//                     if (handler.succeeded()) {
//                         future.complete(handler.result().body());
//                     } else {
//                         LOGGER.info("Error in computing the score");
//                         LOGGER.error("Error in computing the score");
//                         future.complete(JsonObject.of().put("error", handler.cause().getMessage()));
//                     }
//                 });
//         return future;
//     }
// }
