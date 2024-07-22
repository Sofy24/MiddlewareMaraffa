package game.service.schema;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class PlayCardBody {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;
	@JsonProperty(Constants.USERNAME)
	private String username;
	@JsonProperty(Constants.CARD_VALUE)
	private String cardValue;
	@JsonProperty(Constants.CARD_SUIT)
	private String cardSuit;
	@JsonProperty(Constants.IS_SUIT_FINISHED)
	private Boolean isSuitFinished;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof PlayCardBody)) {
			return false;
		}
		final PlayCardBody that = (PlayCardBody) o;
		return this.gameID.equals(that.gameID) && Objects.equals(this.username, that.username)
				&& Objects.equals(this.cardValue, that.cardValue) && Objects.equals(this.cardSuit, that.cardSuit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.gameID, this.username, this.cardValue, this.cardSuit);
	}
}
