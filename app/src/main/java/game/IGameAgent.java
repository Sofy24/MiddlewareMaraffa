package game;

import game.service.User;
import java.util.List;

public interface IGameAgent {

	void onCreateGame(User user);

	void onJoinGame(User user);

	void onStartGame();

	void onCheckMaraffa(int suit, String username);

	void onPlayCard();

	void onTrickCompleted(Trick latestTrick);

	void onMessage();

	void onEndRound();
}
