package org.example.game;

/**An enum with the cards' suits*/
public enum CardSuit {
    CUPS, SWORDS, CLUBS, COINS, NONE;

    public static CardSuit fromUppercaseString(String suit) {
        return switch (suit) {
            case "CUPS" -> CUPS;
            case "SWORDS" -> SWORDS;
            case "CLUBS" -> CLUBS;
            case "COINS" -> COINS;
            case "" -> NONE;
            default -> throw new IllegalArgumentException("Invalid suit: " + suit);
        };
    }
}
