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
}
