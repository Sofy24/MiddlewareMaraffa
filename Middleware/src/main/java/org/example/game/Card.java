package org.example.game;

/**A class modelling the concept of "card"*/
public class Card<X, Y> {
    private X cardValue;
    private Y cardSuit;

    public Card(X cardValue, Y cardSuit) {
        this.cardValue = cardValue;
        this.cardSuit = cardSuit;
    }

    public X getCardValue() {
        return cardValue;
    }

    public Y getCardSuit() {
        return cardSuit;
    }

}
