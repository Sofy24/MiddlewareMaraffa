package game;

import game.service.User;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface IGameAgent {

	void onCreateGame(User user);

	void onJoinGame(User user);

	Future<JsonObject> onStartGame();

	void onPlayCard();

	void onTrickCommpleted(Trick latestTrick ); 

	void onMessage();

	void onEndRound();
}
