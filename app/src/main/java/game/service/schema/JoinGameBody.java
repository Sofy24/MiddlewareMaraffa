package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class JoinGameBody {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;

	@JsonProperty(Constants.USERNAME)
	private String username;

	@JsonProperty(Constants.GUIID)
	private String GUIID;

	public String getGUIID() {
		return this.GUIID;
	}

	public void setGUIID(final String gUIID) {
		this.GUIID = gUIID;
	}

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
		result = prime * result + ((this.gameID == null) ? 0 : this.gameID.hashCode());
		result = prime * result + ((this.username == null) ? 0 : this.username.hashCode());
		result = prime * result + ((this.GUIID == null) ? 0 : this.GUIID.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final JoinGameBody other = (JoinGameBody) obj;
		if (this.gameID == null) {
			if (other.gameID != null)
				return false;
		} else if (!this.gameID.equals(other.gameID))
			return false;
		if (this.username == null) {
			if (other.username != null)
				return false;
		} else if (!this.username.equals(other.username))
			return false;
		if (this.GUIID == null) {
			if (other.GUIID != null)
				return false;
		} else if (!this.GUIID.equals(other.GUIID))
			return false;
		return true;
	}
}
