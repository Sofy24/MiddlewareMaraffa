package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.Objects;

public class StartNewRoundBody {
    @JsonProperty(Constants.GAME_ID)
    private String gameID;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StartNewRoundBody)) return false;
        StartNewRoundBody that = (StartNewRoundBody) o;
        return gameID.equals(that.gameID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID);
    }
}
