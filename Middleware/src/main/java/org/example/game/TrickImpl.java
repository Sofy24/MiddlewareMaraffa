package org.example.game;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.codecs.pojo.annotations.BsonIgnore;

public class TrickImpl implements Trick{
    // private final Map<Card<CardValue, CardSuit>, String> cards = new HashMap<>();
    private final Map<String, String> cards = new HashMap<>();
    @BsonIgnore
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
            this.cards.put(card.toString(), username);
        // }
    }

    @Override
    public Map<String, String> getCards() {
        return this.cards;
    }   

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public CardSuit getLeadingSuit() {
        return leadingSuit;
    }

    @BsonIgnore
    /**@return true if all the players have played their card*/
    @Override
    public boolean isCompleted() {
        return this.cards.values().stream().collect(Collectors.toSet()).size() == this.numberOfPlayers;
    }

}
