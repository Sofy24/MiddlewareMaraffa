package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.Objects;

public class CreateGameBody {
    @JsonProperty(Constants.USERNAME)
    private String username;
    @JsonProperty(Constants.NUMBER_OF_PLAYERS)
    private Integer numberOfPlayers;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(Integer numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CreateGameBody)) return false;
        CreateGameBody that = (CreateGameBody) o;
        return Objects.equals(username, that.username) && Objects.equals(numberOfPlayers, that.numberOfPlayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, numberOfPlayers);
    }
}
