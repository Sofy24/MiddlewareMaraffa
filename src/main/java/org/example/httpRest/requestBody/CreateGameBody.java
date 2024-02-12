package org.example.httpRest.requestBody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateGameBody {
    @JsonProperty("username")
    private String username;
    @JsonProperty("number of the players")
    private int numberOfPlayers;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }
}
