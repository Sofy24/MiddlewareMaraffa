package org.example.game;

public enum GameMode {
    CLASSIC("CLASSIC"), ELEVEN2ZERO("ELEVEN2ZERO");

    public final String name;

    GameMode(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
