package org.example.utils;

import io.vertx.core.http.HttpMethod;

public class Constants {
    //params
    public static final String USERNAME = "username";
    public static final String GAME_ID = "gameID";
    public static final String NUMBER_OF_PLAYERS = "numberOfPlayers";
    public static final String CARD_VALUE = "cardValue";
    public static final String CARD_SUIT = "cardSuit";
    //routes
    public static final String CREATE_GAME = "game/create";
    public static final String JOIN_GAME = "game/join";
    public static final String PLAY_CARD = "game/playCard";
    //methods
    public static final String CREATE_GAME_METHOD = "POST";
    public static final String JOIN_GAME_METHOD = "PATCH";
    public static final String PLAY_CARD_METHOD = "POST";
    //tags
    public static final String GAME_TAG = "Game";
}
