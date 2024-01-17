package org.example.game;

public interface GameApi {
    /** create a game and username is one of the players
     * @param username of the player who has created the game
     * @return the id of the Game (modeled with a Verticle)*/
    String createGame(String username);

    /** the username join the specif game
     * @param username of the player who has created the game
     * @param idGame of the game the user wants to join
     * @return true if success*/
    boolean joinGame(String username, String idGame);

    /** the username played a card
     * @param username of the player who has created the game
     * @param idGame of the game the user wants to join
     * @param card played by the user
     * @return true if success*/
    void playCard(String username, String idGame, Card card); //TODO mi sa che va ritornato lo score
}
