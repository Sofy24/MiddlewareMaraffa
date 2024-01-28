package org.example.game;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import org.example.user.User;

/**TODO/Define the mongodb schema of the game*/
public class GameSchema {
    private String id;
    private List<Trick> tricks;
    public String getId() {
        return id;
    }
    public List<Trick> getTricks() {
        return tricks;
    }
    public GameSchema() {
    }

    public GameSchema(String id) {
        this.id = id;
        this.tricks = new ArrayList<>();
    }

    public void addTrick(Trick trick){
        this.tricks.add(trick);
    }
   
}
