package org.example.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.utils.Constants;

import java.util.Objects;

public class PlayCardBody {
    @JsonProperty(Constants.GAME_ID)
    private String gameID;
    @JsonProperty(Constants.USERNAME)
    private String username;
    @JsonProperty(Constants.CARD_VALUE)
    private Integer cardValue;
    @JsonProperty(Constants.CARD_SUIT)
    private String cardSuit;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayCardBody)) return false;
        PlayCardBody that = (PlayCardBody) o;
        return gameID.equals(that.gameID) && Objects.equals(username, that.username) && Objects.equals(cardValue, that.cardValue) && Objects.equals(cardSuit, that.cardSuit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, username, cardValue, cardSuit);
    }
}
