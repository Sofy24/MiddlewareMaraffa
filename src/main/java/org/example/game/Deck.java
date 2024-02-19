package org.example.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class modelling the concept of "deck"
 */
public class Deck implements IDeck{
    private Map<Card<CardValue, CardSuit>, Boolean> deck; //map

    /**@param intDeck list of cards in integer type
     * @return the map of the card parsed and a boolean, it's true if it's played*/
    public Map<Card<CardValue, CardSuit>, Boolean> fromInt(List<Integer> intDeck) {
        deck = new HashMap<>();
        //intDeck.stream().map(this::mapCard).toList(),false;
        return null;//
    }

    /**@number of the card to parse
     * @return  the related Card*/
    private Card<CardValue, CardSuit> mapCard(int number){
        return switch (number) {
            case 0 -> new Card<>(CardValue.FOUR, CardSuit.COINS);
            case 1 -> new Card<>(CardValue.FIVE, CardSuit.COINS);
            case 2 -> new Card<>(CardValue.SIX, CardSuit.COINS);
            case 3 -> new Card<>(CardValue.SEVEN, CardSuit.COINS);
            case 4 -> new Card<>(CardValue.KNAVE, CardSuit.COINS);
            case 5 -> new Card<>(CardValue.HORSE, CardSuit.COINS);
            case 6 -> new Card<>(CardValue.KING, CardSuit.COINS);
            case 7 -> new Card<>(CardValue.ONE, CardSuit.COINS);
            case 8 -> new Card<>(CardValue.TWO, CardSuit.COINS);
            case 9 -> new Card<>(CardValue.THREE, CardSuit.COINS);

            case 10 -> new Card<>(CardValue.FOUR, CardSuit.CUPS);
            case 11 -> new Card<>(CardValue.FIVE, CardSuit.CUPS);
            case 12 -> new Card<>(CardValue.SIX, CardSuit.CUPS);
            case 13 -> new Card<>(CardValue.SEVEN, CardSuit.CUPS);
            case 14 -> new Card<>(CardValue.KNAVE, CardSuit.CUPS);
            case 15 -> new Card<>(CardValue.HORSE, CardSuit.CUPS);
            case 16 -> new Card<>(CardValue.KING, CardSuit.CUPS);
            case 17 -> new Card<>(CardValue.ONE, CardSuit.CUPS);
            case 18 -> new Card<>(CardValue.TWO, CardSuit.CUPS);
            case 19 -> new Card<>(CardValue.THREE, CardSuit.CUPS);

            case 20 -> new Card<>(CardValue.FOUR, CardSuit.CLUBS);
            case 21 -> new Card<>(CardValue.FIVE, CardSuit.CLUBS);
            case 22 -> new Card<>(CardValue.SIX, CardSuit.CLUBS);
            case 23 -> new Card<>(CardValue.SEVEN, CardSuit.CLUBS);
            case 24 -> new Card<>(CardValue.KNAVE, CardSuit.CLUBS);
            case 25 -> new Card<>(CardValue.HORSE, CardSuit.CLUBS);
            case 26 -> new Card<>(CardValue.KING, CardSuit.CLUBS);
            case 27 -> new Card<>(CardValue.ONE, CardSuit.CLUBS);
            case 28 -> new Card<>(CardValue.TWO, CardSuit.CLUBS);
            case 29 -> new Card<>(CardValue.THREE, CardSuit.CLUBS);

            case 30 -> new Card<>(CardValue.FOUR, CardSuit.SWORDS);
            case 31 -> new Card<>(CardValue.FIVE, CardSuit.SWORDS);
            case 32 -> new Card<>(CardValue.SIX, CardSuit.SWORDS);
            case 33 -> new Card<>(CardValue.SEVEN, CardSuit.SWORDS);
            case 34 -> new Card<>(CardValue.KNAVE, CardSuit.SWORDS);
            case 35 -> new Card<>(CardValue.HORSE, CardSuit.SWORDS);
            case 36 -> new Card<>(CardValue.KING, CardSuit.SWORDS);
            case 37 -> new Card<>(CardValue.ONE, CardSuit.SWORDS);
            case 38 -> new Card<>(CardValue.TWO, CardSuit.SWORDS);
            case 39 -> new Card<>(CardValue.THREE, CardSuit.SWORDS);
            default -> null;
        };
    }

    public List<Card<CardValue, CardSuit>> getDeck() {
        return null; //TODO è null
    }

    /*public void setDeck(List<Card<CardValue, CardSuit>> deck) {
        this.deck = deck;
    }*/
}
