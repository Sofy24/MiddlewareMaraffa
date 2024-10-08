package game.utils;

public class Constants {
    // params
    public static final String USERNAME = "username";
    public static final String GAME_ID = "gameID";
    public static final String NUMBER_OF_PLAYERS = "numberOfPlayers";
    public static final String CARD_VALUE = "cardValue";
    public static final String CARD_SUIT = "cardSuit";
    public static final String TRICK = "trick";
    public static final String CALL = "call";
    public static final String ENDED = "isEnded";
    public static final String EXPECTED_SCORE = "expectedScore";
    public static final String GAME = "games";
    public static final String STATUS = "status";
    public static final String GAME_MODE = "mode";
    public static final String IS_SUIT_FINISHED = "isSuitFinished";
    public static final String COINS_4_USERNAME = "coins4";
    public static final String TEAM = "team";
    public static final String GUIID = "GUIID";
    public static final String POSITION = "position";
    public static final String AUTHOR = "author";
    public static final String PASSWORD = "password";
    // routes
    public static final String CREATE_GAME = "game/create";
    public static final String JOIN_GAME = "game/join";
    public static final String START_GAME = "game/start";
    public static final String PLAYER_CARDS = "game/:" + GAME_ID + "/:" + USERNAME + "/cards";
    public static final String PLAY_CARD = "round/playCard";
    public static final String CAN_START = "round/canStart/:" + GAME_ID;
    public static final String CHOOSE_TRUMP = "round/chooseTrump";
    public static final String START_NEW_ROUND = "round/startNewRound";
    public static final String CHANGE_TEAM = "game/changeTeam";
    public static final String STATE = "game/state/:" + GAME_ID;
    public static final String CARDS_ON_HAND = "round/cardsOnHand/:" + GAME_ID;
    public static final String CARDS_ON_TABLE = "round/cardsOnTable/:" + GAME_ID;
    public static final String END_ROUND = "round/end/:" + GAME_ID;
    public static final String END_GAME = "game/end/:" + GAME_ID;
    public static final String MAKE_CALL = "round/makeCall";
    public static final String GAMES = "game/getGames";
    public static final String GETGAME = "game/:" + GAME_ID;
    public static final String COINS_4 = "game/4Coins/:" + GAME_ID + "/username:" + COINS_4_USERNAME;
    public static final String GET_PLAYERS = "player";
    public static final String NEW_GAME = "game/newGame";
    public static final String SET_PASSWORD = "game/password";
    public static final String REMOVE_USER = "game/remove";
	public static final String GET_TOTAL_GAMES = "game/count";

    // methods
    public static final String PLAYERS_METHOD = "GET";
    public static final String CREATE_GAME_METHOD = "POST";
    public static final String NEW_GAME_METHOD = "POST";
    public static final String JOIN_GAME_METHOD = "PATCH";
    public static final String START_GAME_METHOD = "PATCH";
    public static final String PLAY_CARD_METHOD = "POST";
    public static final String CAN_START_METHOD = "GET";
    public static final String GET_PLAYER_CARD_METHOD = "GET";
    public static final String CHOOSE_TRUMP_METHOD = "POST";
    public static final String START_NEW_ROUND_METHOD = "PATCH";
    public static final String CHANGE_TEAM_METHOD = "PATCH";
    public static final String STATE_METHOD = "GET";
    public static final String CARDS_ON_HAND_METHOD = "GET";
    public static final String CARDS_ON_TABLE_METHOD = "GET";
    public static final String END_METHOD = "GET";
    public static final String MAKE_CALL_METHOD = "POST";
    public static final String GAMES_METHOD = "GET";
    public static final String COINS_4_METHOD = "GET";
    public static final String GET_TOTAL_GAMES_METHOD = "GET";
    public static final String EXIT_GAME = "DELETE";
    public static final String PASSOWRD_METHOD = "PATCH";
    public static final String REMOVE_USER_METHOD = "PATCH";

	// tags
	public static final String GAME_TAG = "Middleware.Game";
	public static final String ROUND_TAG = "Middleware.Round";
	// json attributes
	public static final String START_ATTR = "start";
	public static final String JOIN_ATTR = "join";
	public static final String NOT_FOUND = "not found";
	public static final String FULL = "full";
	public static final String MESSAGE = "message";
	public static final String ALREADY_JOINED = "alreadyJoined";
	public static final String ILLEGAL_TRUMP = "illegalTrump";
	public static final String TRUMP = "trump";
	public static final String SUIT = "suit";
	public static final String PLAY = "play";
	public static final String DECK = "deck";
	public static final String INVALID = "invalid";
	public static final String NOT_ALLOWED = "notAllowed";
	public static final String TURN = "turn";
	public static final String RESULT = "result";
	public static final String VALUE = "value";
	public static final String ERROR = "error";
	public static final String GUEST = "guest";
	public static final String TOTAL = "total";
	public static final String NEW_GAME_CREATION = "newGame";
	// game constants
	public static final int NUMBER_OF_CARDS = 40;
	public static final int MARAFFA_SCORE = 9;
	public static final int ELEVEN_ZERO_SCORE = 11;
	public static final String CLOSED = "closed";

}
