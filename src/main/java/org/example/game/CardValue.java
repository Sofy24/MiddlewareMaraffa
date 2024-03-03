package org.example.game;

/** An enum with the values of the cards */
public enum CardValue {
    ONE(7), TWO(8), THREE(9), FOUR(0), FIVE(1), SIX(2), SEVEN(3), KNAVE(4), HORSE(5), KING(6), NONE(999);

    public final Integer value;

    CardValue(Integer value) {
        this.value = value;
    }

    public Integer getValue(){
        return value;
    }

    public static CardValue getName(String value) {
        for(CardValue v : values())
            if(v.getValue().equals(Integer.parseInt(value))) return v;
        System.out.println("Oh oh oh oh oh, value = " + value);
        throw new IllegalArgumentException();
    }
}
