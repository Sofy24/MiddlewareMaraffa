package game;

import java.util.UUID;

public interface GameApi {
    /** create a game and username is one of the players
     * @param username of the player who has created the game
     * @return the id of the Game (modeled with a Verticle)*/
    UUID createGame(String username, int numberOfPlayers, int expectedScore, GameMode gameMode);

    /** the username join the specif game
     * @param username of the player who has created the game
     * @param idGame of the game the user wants to join
     * @return true if success*/
    boolean joinGame(String username, int idGame);

    /** the username played a card
     * @param idGame of the game the user wants to join
     * @param card played by the user
     * @param username user who played the card
     * @return */
    boolean playCard(int idGame, Card<CardValue, CardSuit> card, String username); //TODO mi sa che va ritornato lo score

    /**
     * @param idGame the game to check
     * @return true if all players have joined the game
     * */
    boolean canStart(int idGame);

    /**@param idGame the specific game
     * @param suit the suit choosen by the first player */
    void chooseSuit(int idGame, CardSuit suit);

    /**@param idGame the specific game
     * reset the leading suit in order to start a new round*/
    void startNewRound(int idGame);
}
