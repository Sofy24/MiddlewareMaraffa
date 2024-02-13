package org.example.service.requestBody;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class JoinGameBody {
    @JsonProperty("gameID")
    private String gameID;
    @JsonProperty("username")
    private String username;

    public String getGameID() {
        return gameID;
    }

    public void setGameID(String gameID) {
        this.gameID = gameID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinGameBody that)) return false;
        return Objects.equals(username, that.username) && Objects.equals(gameID, that.gameID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, gameID);
    }
}
