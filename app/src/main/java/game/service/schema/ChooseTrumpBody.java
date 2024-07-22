package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;


public class ChooseTrumpBody {
    @JsonProperty(Constants.GAME_ID)
    private String gameID;
    @JsonProperty(Constants.USERNAME)
    private String username;
    @JsonProperty(Constants.CARD_SUIT)
    private String cardSuit;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.gameID == null ? 0 : this.gameID.hashCode());
        result = prime * result + (this.cardSuit == null ? 0 : this.cardSuit.hashCode());
        result = prime * result + (this.username == null ? 0 : this.username.hashCode());
        return result;
    }
    @Override
    public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
        final ChooseTrumpBody other = (ChooseTrumpBody) obj;
        if (this.gameID == null) {
			if (other.gameID != null) {
				return false;
			}
        } else if (!this.gameID.equals(other.gameID)) {
			return false;
		}
        if (this.cardSuit == null) {
			if (other.cardSuit != null) {
				return false;
			}
        } else if (!this.cardSuit.equals(other.cardSuit)) {
			return false;
		}
        if (this.username == null) {
			if (other.username != null) {
				return false;
			}
        } else if (!this.username.equals(other.username)) {
			return false;
		}
        return true;
    }


}
