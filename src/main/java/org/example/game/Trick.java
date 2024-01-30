package org.example.game;

import java.util.List;
import java.util.Map;

public interface Trick {
    /**add a card played by a player
     * @param card played*/
    void addCard(Card<CardValue, CardSuit> card, String username);

    /**check if the trick is completed
     * @return true if all the players have played their card
     */
    boolean isCompleted();

    /**@return the cards in the trick*/
    // List<Card<CardValue, CardSuit>> getCards();
    Map<String, String> getCards();
}
