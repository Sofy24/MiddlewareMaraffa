package org.example.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bson.codecs.pojo.annotations.BsonIgnore;

public class TrickImpl implements Trick{
    // private final Map<Card<CardValue, CardSuit>, String> cards = new HashMap<>();
    private final Map<String, String> cards = new HashMap<>();
    //private //metti la call
    @BsonIgnore
    private final int numberOfPlayers;

    @BsonIgnore
    private CardSuit trump;

    public TrickImpl(int numberOfPlayers, final CardSuit trump) {
        this.numberOfPlayers = numberOfPlayers;
        this.trump = trump;
    }

    /**@param card added to the trick, if not all the players has already played*/
    @Override
    public void addCard(Card<CardValue, CardSuit> card, String username) {
        this.cards.put(card.toString(), username);
    }

    @Override
    public Map<String, String> getCards() {
        return this.cards;
    }   

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public CardSuit getTrump() {
        return trump;
    }

    @BsonIgnore
    /**@return true if all the players have played their card*/
    @Override
    public boolean isCompleted() {
        return new HashSet<>(this.cards.values()).size() == this.numberOfPlayers;
    }

    @Override
    public String toString() {
        return "Trick{" +
                "cards=" + cards +
                ", trump=" + trump +
                '}';
    }
}
