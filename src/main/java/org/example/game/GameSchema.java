package org.example.game;

import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import org.example.user.User;

/**TODO/Define the mongodb schema of the game*/
public class GameSchema {
    private String gameID;
    private CardSuit leadingSuit;
    public void setLeadingSuit(CardSuit leadingSuit) {
        this.leadingSuit = leadingSuit;
    }

    public CardSuit getLeadingSuit() {
        return leadingSuit;
    }
    private List<Trick> tricks;

    public String getGameID() {
        return gameID;
    }

    public List<Trick> getTricks() {
        return tricks;
    }
    public GameSchema() {
    }

    public GameSchema(String identifier, CardSuit leadingSuit) {
        this.gameID = identifier;
        this.leadingSuit = leadingSuit;
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
