package org.example.utils;


import io.vertx.core.http.HttpMethod;

public class Constants {
    //params
    public static final String USERNAME = "username";
    public static final String GAME_ID = "gameID";
    public static final String NUMBER_OF_PLAYERS = "numberOfPlayers";
    public static final String CARD_VALUE = "cardValue";
    public static final String CARD_SUIT = "cardSuit";
    public static final String TRICK = "trick";
    public static final String CALL = "call";
    //routes
    public static final String CREATE_GAME = "game/create";
    public static final String JOIN_GAME = "game/join";
    public static final String PLAY_CARD = "game/playCard";
    public static final String CAN_START = "game/canStart/:" + GAME_ID;
    public static final String CHOOSE_TRUMP = "game/chooseTrump";
    public static final String START_NEW_ROUND = "game/startNewRound";
    public static final String STATE = "game/state";
    public static final String CARDS_ON_HAND = "game/cardsOnHand";
    public static final String CARDS_ON_TABLE = "game/cardsOnTable";
    public static final String END = "game/end";
    public static final String MAKE_CALL = "game/makeCall";
    //methods
    public static final String CREATE_GAME_METHOD = "POST";
    public static final String JOIN_GAME_METHOD = "PATCH";
    public static final String PLAY_CARD_METHOD = "POST";
    public static final String CAN_START_METHOD = "GET";
    public static final String CHOOSE_TRUMP_METHOD = "POST";
    public static final String START_NEW_ROUND_METHOD = "PATCH";
    public static final String STATE_METHOD = "GET";
    public static final String CARDS_ON_HAND_METHOD = "GET";
    public static final String CARDS_ON_TABLE_METHOD = "GET";
    public static final String END_METHOD = "GET";
    public static final String MAKE_CALL_METHOD = "POST";
    //tags
    public static final String GAME_TAG = "Game";
    //json attributes
    public static final String CAN_START_ATTR = "canStart";
    public static final String JOIN_ATTR = "join";
    public static final String NOT_FOUND = "notFound";
    public static final String FULL = "full";
    public static final String MESSAGE = "message";
    public static final String ALREADY_JOINED = "alreadyJoined";
    public static final String ILLEGAL_TRUMP = "illegalTrump";
    public static final String TRUMP = "trump";

}
