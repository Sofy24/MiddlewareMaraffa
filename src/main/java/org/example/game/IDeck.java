package org.example.game;

import java.util.List;
import java.util.Map;

public interface IDeck {
    /**@param intDeck list of cards in integer type
     * @return the list of the card parsed*/
    Map<Card<CardValue, CardSuit>, Boolean> fromInt(List<Integer> intDeck);
}
