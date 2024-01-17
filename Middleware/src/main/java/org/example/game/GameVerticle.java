package org.example.game;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***This class models a game using a Verticle from vertx.
 * id = the id of the verticle
 * numberOfPlayers = the numbers of players of this game
 * stateMap = it saves each state with the related trick
 * users = it keeps track of all the users added to the game*/
public class GameVerticle extends AbstractVerticle {
    private final int id;
    private final int numberOfPlayers;
    private Map<String, Trick> stateMap = new ConcurrentHashMap<>();

    private final List<String> users = new ArrayList<>();

    public GameVerticle(int id, String username, int numberOfPlayers) {
        this.id = id;
        this.numberOfPlayers = numberOfPlayers;
        users.add(username);

    }

    /**It starts the verticle*/
    @Override
    public void start(Promise<Void> startPromise){

        startPromise.complete();
    }

    public int getId() {
        return id;
    }

    public Map<String, Trick> getStateMap() {
        return stateMap;
    }

    private void setStateMap(Map<String, Trick> stateMap) {
        this.stateMap = stateMap;
    }

    public boolean addUser(String username) {
        if (this.users.size() < this.numberOfPlayers && !this.users.contains(username)) {
            this.users.add(username);
            return true;
        }
        return false;
    }
}
