package org.example.game;

/**
 * A class modelling the concept of "card"
 */
public record Card<X, Y>(X cardValue, Y cardSuit) {
    @Override
    public String toString() {
        return "Card [cardValue=" + cardValue + ", cardSuit=" + cardSuit + "]";
    }
}