package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

public class CreateGameBody {
    @JsonProperty(Constants.USERNAME)
    private String username;
    @JsonProperty(Constants.NUMBER_OF_PLAYERS)
    private Integer numberOfPlayers;

    public CreateGameBody(String username, Integer numberOfPlayers) {
        this.username = username;
        this.numberOfPlayers = numberOfPlayers;
    }

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + numberOfPlayers;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CreateGameBody other = (CreateGameBody) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        if (numberOfPlayers != other.numberOfPlayers)
            return false;
        return true;
    }

}
