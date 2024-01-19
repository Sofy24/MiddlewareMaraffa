package org.example.game;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/***This class models a game using a Verticle from vertx.
 * id = the id of the verticle
 * numberOfPlayers = the numbers of players of this game
 * stateMap = it saves each state with the related trick
 * users = it keeps track of all the users added to the game*/
public class GameVerticle extends AbstractVerticle {
    private final int id;

    private final AtomicInteger currentState;
    private final int numberOfPlayers;

    private CardSuit leadingSuit = CardSuit.NONE;
    private Map<Integer, Trick> states = new ConcurrentHashMap<>();

    private final List<String> users = new ArrayList<>();

    private Trick currentTrick;

    public GameVerticle(int id, String username, int numberOfPlayers) {
        this.id = id;
        this.currentState = new AtomicInteger(0);
        this.numberOfPlayers = numberOfPlayers;
        users.add(username);

    }

    /**It starts the verticle*/
    @Override
    public void start(Promise<Void> startPromise){

        startPromise.complete();
    }

    /**@return true if the user is added*/
    public boolean addUser(String username) {
        if (this.users.size() < this.numberOfPlayers && !this.users.contains(username)) {
            this.users.add(username);
            return true;
        }
        return false;
    }

    /** Adds the card if the trick is not completed, otherwise it adds the card to a new trick and updates the
     * current state
     * @param card to be added to the trick*/
    public boolean addCard(Card<CardValue, CardSuit> card) {
        if (canStart()){
        this.currentTrick = this.states.getOrDefault(this.currentState.get(), new TrickImpl(this.numberOfPlayers));
        if (!currentTrick.isCompleted()){
            currentTrick.addCard(card);
        } else {
            currentTrick = new TrickImpl(this.numberOfPlayers);
            currentTrick.addCard(card);
            this.states.put(this.currentState.incrementAndGet(), currentTrick);
        }
        return true;
        } else {
            return false;
        }
    }

    /**@return true if all players have joined the game*/
    public boolean canStart() {
        return this.users.size() == this.numberOfPlayers && !this.leadingSuit.equals(CardSuit.NONE);
    }

    /**@param suit the leading suit of the round*/
    public void chooseSuit(CardSuit suit){
        this.leadingSuit = suit;
    }

    public int getId() {
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

}
