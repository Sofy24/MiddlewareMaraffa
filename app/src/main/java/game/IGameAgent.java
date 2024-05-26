package game;

import game.service.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.CompletableFuture;

public interface IGameAgent {

	void onCreateGame(User user);

	void onJoinGame(User user);

	Future<JsonObject> onStartGame();

	void onPlayCard();

	CompletableFuture<JsonObject> onTrickCompleted(Trick latestTrick ); 

	void onMessage();

	void onEndRound();
}
