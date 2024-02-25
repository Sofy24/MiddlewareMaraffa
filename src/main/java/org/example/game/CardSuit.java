package org.example.game;

/** An enum with the cards' suits */
public enum CardSuit {
    CUPS(1), SWORDS(3), CLUBS(2), COINS(0), NONE(999);

    public final Integer value;

    private CardSuit(Integer value) {
        this.value = value;
    }

    public Integer getValue(){
        return value;
    }
}
