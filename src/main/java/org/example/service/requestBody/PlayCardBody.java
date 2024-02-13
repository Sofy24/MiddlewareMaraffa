package org.example.service.requestBody;

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

    /*public String getGameID() {
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

    public Integer getCardValue() {
        return cardValue;
    }

    public void setCardValue(Integer cardValue) {
        this.cardValue = cardValue;
    }

    public String getCardSuit() {
        return cardSuit;
    }

    public void setCardSuit(String cardSuit) {
        this.cardSuit = cardSuit;
    }

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
    }*/
}
