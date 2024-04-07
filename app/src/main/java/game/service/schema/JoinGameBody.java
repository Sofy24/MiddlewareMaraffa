package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;
import java.util.Objects;

public class JoinGameBody {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;

	@JsonProperty(Constants.USERNAME)
	private String username;

	public String getGameID() {
		return gameID;
	}

	public void setGameID(final String gameID) {
		this.gameID = gameID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof final JoinGameBody that))
			return false;
		return Objects.equals(username, that.username) && Objects.equals(gameID, that.gameID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, gameID);
	}
}
