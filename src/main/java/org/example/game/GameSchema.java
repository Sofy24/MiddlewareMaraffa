package org.example.game;

import java.util.ArrayList;
import java.util.List;

/**TODO/Define the mongodb schema of the game*/
public class GameSchema {
    private String gameID;
    private CardSuit trump;
    public void setTrump(CardSuit trump) {
        this.trump = trump;
    }

    public CardSuit getTrump() {
        return trump;
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

    public GameSchema(String identifier, CardSuit trump) {
        this.gameID = identifier;
        this.trump = trump;
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
