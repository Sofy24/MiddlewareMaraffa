package game;

import game.service.User;
import java.util.List;

/*
 * An interface for the game agent 
 */
public interface IGameAgent {

	void onCreateGame(User user);

	void onNewRound();

	void onJoinGame(User user);

	void onStartGame();

	void onCheckMaraffa(int suit, String username);

	void onPlayCard();

	void onTrickCompleted(Trick latestTrick);

	void onMessage();

	void onEndRound();

	void onEndGame();

	void onNewGame(String newGameID);

	void onChangeTeam();

	void onMakeCall(final Call call);

	void onRemoveUser();
}
