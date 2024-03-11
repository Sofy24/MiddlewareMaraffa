package org.example.game;

/**
 * A record modelling the concept of "card"
 */
public record Card<X, Y> (CardValue cardValue, CardSuit cardSuit) {
    
    @Override
    public String toString() {
        return "Card [" + cardValue + ", " + cardSuit + "]";
    }

    public CardValue cardValue() {
        return cardValue;
    }

    public CardSuit cardSuit() {
        return cardSuit;
    }

    public Integer getCardValue() {
        return cardSuit.value  * 10 + cardValue.value;
    }
}
