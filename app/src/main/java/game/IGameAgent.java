package game;

import game.service.User;

public interface IGameAgent {

	void onCreateGame(User user);

	void onJoinGame(User user);

	void onStartGame();

	void onPlayCard();

	void onMessage();

	void onEndRound();
}
