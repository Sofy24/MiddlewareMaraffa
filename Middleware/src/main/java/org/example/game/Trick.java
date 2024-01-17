package org.example.game;

public interface Trick {
    /**add a card played by a player
     * @param card played*/
    void addCard(Card card);

    /**check if the trick is completed
     * @return true if all the players have played their card
     */
    boolean isCompleted();
}
