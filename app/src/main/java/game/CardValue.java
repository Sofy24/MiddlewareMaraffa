package game;

/** An enum with the values of the cards */
public enum CardValue {
	ONE(7), TWO(8), THREE(9), FOUR(0), FIVE(1), SIX(2), SEVEN(3), KNAVE(4), HORSE(5), KING(6), NONE(999);

	public final Integer value;

	CardValue(final Integer value) {
		this.value = value;
	}

	public Integer getValue() {
		return this.value;
	}

	public static CardValue fromValue(final int value) {
		for (final CardValue v : values())
			if (v.value.equals(value)) {
				return v;
			}
		throw new IllegalArgumentException();
	}

	public static CardValue getName(final String value) {
		for (final CardValue v : values())
			if (v.toString().equals(value)) {
				return v;
			}
		throw new IllegalArgumentException();
	}
}
