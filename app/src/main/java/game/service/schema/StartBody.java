package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;
import java.util.Objects;

public class StartBody {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;

	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StartBody)) {
			return false;
		}
		StartBody startBody = (StartBody) o;
		return gameID.equals(startBody.gameID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gameID);
	}
}
