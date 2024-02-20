package org.example.game;

/**An enum with the values of the cards*/
public enum CardValue {
    ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, KNAVE, HORSE, KING, NONE;

    public static CardValue fromInteger(Integer number) {
        return switch (number) {
            case 1 -> ONE;
            case 2 -> TWO;
            case 3 -> THREE;
            case 4 -> FOUR;
            case 5 -> FIVE;
            case 6 -> SIX;
            case 7 -> SEVEN;
            case 8 -> KNAVE;
            case 9 -> HORSE;
            case 10 -> KING;
            default -> NONE;
        };
    }
}

