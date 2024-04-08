package game;

import game.service.User;

public interface IGameAgent {

    void onCreateGame();

    void onJoinGame(User user);

    void onStartGame();

    void onPlayCard();

    void onMessage();

    void onEndRound();
}
