package org.example.game;

public interface GameApi {
    /** create a game and username is one of the players
     * @param username of the player who has created the game
     * @return the id of the Game (modeled with a Verticle)*/
    int createGame(String username, int numberOfPlayers);

    /** the username join the specif game
     * @param username of the player who has created the game
     * @param idGame of the game the user wants to join
     * @return true if success*/
    boolean joinGame(String username, int idGame);

    /** the username played a card
     * @param idGame of the game the user wants to join
     * @param card played by the user
     * @return */
    boolean playCard(int idGame, Card<CardValue, CardSuit> card); //TODO mi sa che va ritornato lo score

    /**
     * @param idGame the game to check
     * @return true if all players have joined the game
     * */
    boolean CanStart(int idGame);
}
