package org.example.game;

import org.example.utils.Pair;

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
    // Map<String, Pair<Integer, String>> getCards();
    Map<String, String> getCards();

    /**@param call the call
     * @param username the user who makes the call
     * check if the user can make a call and if true set the call*/
    void setCall(Call call, String username);

    /**@return the call of this trick*/
    Call getCall();
}
