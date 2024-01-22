package org.example.game;

import java.util.ArrayList;
import java.util.List;

public class TrickImpl implements Trick{
    private final List<Card<CardValue, CardSuit>> cards = new ArrayList<>();
    private final int numberOfPlayers;

    public TrickImpl(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    /**@param card added to the trick, if not all the players has already played*/
    @Override
    public void addCard(Card<CardValue, CardSuit> card) {
        if(!isCompleted()){
            this.cards.add(card);
        }
    }

    /**@return true if all the players have played their card*/
    @Override
    public boolean isCompleted() {
        return this.cards.size() == this.numberOfPlayers;
    }


    public List<Card<CardValue, CardSuit>> getCards() {
        return this.cards;
    }

}
