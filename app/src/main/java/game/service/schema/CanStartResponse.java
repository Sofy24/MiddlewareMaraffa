package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;
import java.util.Objects;

public class CanStartResponse {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CanStartResponse))
			return false;
		final CanStartResponse that = (CanStartResponse) o;
		return gameID.equals(that.gameID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gameID);
	}
}
