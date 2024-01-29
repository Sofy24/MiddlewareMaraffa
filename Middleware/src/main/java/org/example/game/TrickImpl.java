package org.example.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TrickImpl implements Trick{
    private final Map<Card<CardValue, CardSuit>, String> cards = new HashMap<>();
    private final int numberOfPlayers;
    private CardSuit leadingSuit;

    public TrickImpl(int numberOfPlayers, final CardSuit leadingSuit) {
        this.numberOfPlayers = numberOfPlayers;
        this.leadingSuit = leadingSuit;
    }

    /**@param card added to the trick, if not all the players has already played*/
    @Override
    public void addCard(Card<CardValue, CardSuit> card, String username) {
        // NON serve questo controllo
        // if(!isCompleted()){ 
            this.cards.put(card, username);
        // }
    }

    @Override
    public Map<Card<CardValue, CardSuit>, String> getCards() {
        return this.cards;
    }   

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public CardSuit getLeadingSuit() {
        return leadingSuit;
    }

    /**@return true if all the players have played their card*/
    @Override
    public boolean isCompleted() {
        return this.cards.values().stream().collect(Collectors.toSet()).size() == this.numberOfPlayers;
    }

}
