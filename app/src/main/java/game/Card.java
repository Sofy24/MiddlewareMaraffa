package game;

/** A record modelling the concept of "card" */
public record Card<X, Y>(CardValue cardValue, CardSuit cardSuit) {

    @Override
    public String toString() {
        return "Card [" + this.cardValue + ", " + this.cardSuit + "]";
    }

    public CardValue cardValue() {
        return this.cardValue;
    }

    public CardSuit cardSuit() {
        return this.cardSuit;
    }

    public Integer getCardValue() {
        return this.cardSuit.value * 10 + this.cardValue.value;
    }

    static Card<CardValue, CardSuit> fromInteger(final int value) {
        final var cardSuit = value / 10;
        final var cardValue = value - cardSuit * 10;
        return new Card<CardValue, CardSuit>(CardValue.fromValue(cardValue), CardSuit.fromValue(cardSuit));
        // return new Card<CardValue,
        // CardSuit>(CardValue.getName(String.valueOf(cardValue)),
        // CardSuit.getName(String.valueOf(cardSuit)));
    }
}
