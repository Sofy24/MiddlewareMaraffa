package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;
import java.util.Objects;

public class ChooseTrumpBody {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;

	@JsonProperty(Constants.CARD_SUIT)
	private String cardSuit;

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ChooseTrumpBody))
			return false;
		final ChooseTrumpBody that = (ChooseTrumpBody) o;
		return Objects.equals(gameID, that.gameID) && Objects.equals(cardSuit, that.cardSuit);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gameID, cardSuit);
	}
}
