package org.example.game;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.example.repository.AbstractStatisticManager;
import org.example.utils.Constants;

import static java.lang.Math.floor;

/***
 * This class models a game using a Verticle from vertx.
 * id = the id of the verticle
 * numberOfPlayers = the numbers of players of this game
 * stateMap = it saves each state with the related trick
 * users = it keeps track of all the users added to the game
 */
public class GameVerticle extends AbstractVerticle {
    private final UUID id;
    private final AtomicInteger currentState;
    private final int numberOfPlayers;
    private final IDeck deck = new Deck();
    private CardSuit trump = CardSuit.NONE;
    private Map<Integer, Trick> states = new ConcurrentHashMap<>(); //quando lo stato arriva a numerocarte/numerogiocatori
    private final List<String> users = new ArrayList<>();
    private final GameSchema gameSchema;
    private AbstractStatisticManager statisticManager;
    private Trick currentTrick;

    public GameSchema getGameSchema() {
        return gameSchema;
    }

    public GameVerticle(UUID id, String username, int numberOfPlayers, AbstractStatisticManager statisticManager) {
        this.id = id;
        this.currentState = new AtomicInteger(0);
        this.numberOfPlayers = numberOfPlayers;
        users.add(username);
        this.gameSchema = new GameSchema(String.valueOf(id), CardSuit.NONE);
        this.statisticManager = statisticManager;
        if(this.statisticManager != null) this.statisticManager.createRecord(this.gameSchema); //TODO andrebbero usati gli UUID ma vediamo se mongo di aiuta con la questione _id
    }

    public GameVerticle(UUID id, String username, int numberOfPlayers) {
        this.id = id;
        this.currentState = new AtomicInteger(0);
        this.numberOfPlayers = numberOfPlayers;
        users.add(username);
        this.gameSchema = new GameSchema(String.valueOf(id), CardSuit.NONE);
    }

    /** It starts the verticle */
    @Override
    public void start(Promise<Void> startPromise) {

        startPromise.complete();
    }

    /** @return true if the user is added */
    public boolean addUser(String username) {
        if (!this.users.contains(username)) {
            this.users.add(username);
            return true;
        }
        return false;
    }

    /**
     * Adds the card if the trick is not completed, otherwise it adds the card to a
     * new trick and updates the
     * current state
     * 
     * @param card to be added to the trick
     */
    public boolean addCard(Card<CardValue, CardSuit> card, String username) {
        if (canStart()) {
            if(this.currentTrick == null){
                this.currentTrick = this.states.getOrDefault(this.currentState.get(),
                    new TrickImpl(this.numberOfPlayers, this.trump));
            } 
            currentTrick.addCard(card, username);
            if (currentTrick.isCompleted()) {
                this.gameSchema.addTrick(currentTrick);
                if(this.statisticManager != null) this.statisticManager.updateRecordWithTrick(String.valueOf(id), currentTrick);
                this.states.put(this.currentState.incrementAndGet(), currentTrick);
                currentTrick = this.trump == CardSuit.NONE ? null : new TrickImpl(this.numberOfPlayers, this.trump);
            }
            return true;
        }
        return false;
    }

    /** @return true if all players have joined the game */
    public boolean canStart() {
        return this.users.size() == this.numberOfPlayers; // && !this.trump.equals(CardSuit.NONE);
    }

    /** @param suit the leading suit of the round */
    public void chooseTrump(CardSuit suit) {
        this.trump = suit;
        this.gameSchema.setTrump(suit);
        if(this.statisticManager != null) this.statisticManager.updateSuit(this.gameSchema); //TODO serve davvero o soltanto roba che sembra utile ? 
    }

    /** reset the trump */
    public void startNewRound() {
        this.chooseTrump(CardSuit.NONE);
    }

    /**@param call the call
     * @param username the user who makes the call
     * @return true if the call is made correctly */
    public boolean makeCall(Call call, String username){
        if (currentTrick == null){
            this.currentTrick = this.states.getOrDefault(this.currentState.get(),
                    new TrickImpl(this.numberOfPlayers, this.trump));
        }
        if (users.get(0).equals(username)){
            this.currentTrick.setCall(call, username);
        }
        return !this.currentTrick.getCall().equals(Call.NONE);
    }

    public UUID getId() {
        return id;
    }

    public Map<Integer, Trick> getStates() {
        return states;
    }

    private void setStates(Map<Integer, Trick> states) {
        this.states = states;
    }

    public AtomicInteger getCurrentState() {
        return currentState;
    }

    public Trick getCurrentTrick() {
        return currentTrick;
    }

    public CardSuit getTrump() {
        return trump;
    }

    /**@return the number of players who have already joined the game*/
    public int getNumberOfPlayersIn(){
        return this.users.size();
    }

    /**@return the number of players for this game*/
    public int getMaxNumberOfPlayers(){
        return this.numberOfPlayers;
    }

    /**@return true if the round is a*/
    public boolean isRoundEnded(){
        double numberOfTricksInRound = floor((float) Constants.NUMBER_OF_CARDS / this.numberOfPlayers);
        return this.currentState.get() == numberOfTricksInRound;
    }
}
