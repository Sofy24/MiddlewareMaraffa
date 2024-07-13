package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;
import java.util.Objects;

public class NewGameBody {
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
		if (!(o instanceof NewGameBody)) {
			return false;
		}
			NewGameBody newGameBody = (NewGameBody) o;
		return gameID.equals(newGameBody.gameID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gameID);
	}
}
