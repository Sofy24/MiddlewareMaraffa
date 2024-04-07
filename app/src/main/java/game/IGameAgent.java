package game;

public interface IGameAgent {

    void onCreateGame();

    void onJoinGame(String username);

    void onStartGame();

    void onPlayCard();

    void onMessage();

    void onEndRound();
}
