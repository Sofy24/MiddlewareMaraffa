package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class RemoveUserBody {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;

	@JsonProperty(Constants.USERNAME)
	private String username;



	public String getGameID() {
		return this.gameID;
	}

	public void setGameID(final String gameID) {
		this.gameID = gameID;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameID == null) ? 0 : gameID.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoveUserBody other = (RemoveUserBody) obj;
		if (gameID == null) {
			if (other.gameID != null)
				return false;
		} else if (!gameID.equals(other.gameID))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	


}