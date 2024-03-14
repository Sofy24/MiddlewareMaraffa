package game;

/** An enum with the cards' suits */
public enum CardSuit {
    CUPS(1), SWORDS(3), CLUBS(2), COINS(0), NONE(999);

    public final Integer value;

    CardSuit(Integer value) {
        this.value = value;
    }

    public Integer getValue(){
        return value;
    }

    public static CardSuit getName(String value) {
        for(CardSuit v : values())
            if(v.toString().equals(value)) return v;
        System.out.println("Oh oh oh oh oh, value = " + value);
        throw new IllegalArgumentException();
    }
}
