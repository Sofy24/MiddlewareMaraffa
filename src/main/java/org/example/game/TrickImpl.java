package org.example.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.bson.codecs.pojo.annotations.BsonIgnore;

public class TrickImpl implements Trick{
    private final Map<String, String> cards = new HashMap<>();

    private Call call = Call.NONE;
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

    public Call getCall() {
        return call;
    }

    public void setCall(Call call, String username) {
        //if (username.equals(this.cards.get(0)))
        this.call = call;
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

    /**@return true if all the players have played their card*/
    @BsonIgnore
    @Override
    public boolean isCompleted() {
        return this.cards.keySet().size() == this.numberOfPlayers;
    }

    @Override
    public String toString() {
        return "Trick{" +
                "cards=" + cards +
                ", trump=" + trump +
                ", call=" + call +
                '}';
    }
}
