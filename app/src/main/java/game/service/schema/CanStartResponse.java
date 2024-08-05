package game.service.schema;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class CanStartResponse {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CanStartResponse)) {
			return false;
		}
		final CanStartResponse that = (CanStartResponse) o;
		return this.gameID.equals(that.gameID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.gameID);
	}
}
