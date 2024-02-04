package org.example.game;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import org.example.user.User;

/**TODO/Define the mongodb schema of the game*/
public class GameSchema {
    private String gameID;
    public String getGameID() {
        return gameID;
    }
  
    private List<Trick> tricks;

    public List<Trick> getTricks() {
        return tricks;
    }
    public GameSchema() {
    }

    public GameSchema(String identifier) {
        this.gameID = identifier;
        this.tricks = new ArrayList<>();
    }

    public void addTrick(Trick trick){
        this.tricks.add(trick);
    }
    @Override
    public String toString() {
        return "GameSchema [gameID=" + gameID + ", tricks=" + tricks + "]";
    }
   
}
