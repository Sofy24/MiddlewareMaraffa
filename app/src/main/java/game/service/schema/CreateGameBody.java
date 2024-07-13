package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;

public class CreateGameBody {
	@JsonProperty(Constants.USERNAME)
	private String username;

	@JsonProperty(Constants.NUMBER_OF_PLAYERS)
	private Integer numberOfPlayers;

	@JsonProperty(Constants.EXPECTED_SCORE)
	private Integer expectedScore;

	@JsonProperty(Constants.GAME_MODE)
	private String gameMode;

	@JsonProperty(Constants.PASSWORD)
	private String password;

	@JsonProperty(Constants.GUIID)
	private String GUIID;

	public String getGUIID() {
		return this.GUIID;
	}

	public void setGUIID(final String gUIID) {
		this.GUIID = gUIID;
	}

	public Integer getExpectedScore() {
		return this.expectedScore;
	}

	public void setExpectedScore(final Integer expectedScore) {
		this.expectedScore = expectedScore;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public Integer getNumberOfPlayers() {
		return this.numberOfPlayers;
	}

	public void setNumberOfPlayers(final Integer numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}

	

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.username == null) ? 0 : this.username.hashCode());
		result = prime * result + ((this.numberOfPlayers == null) ? 0 : this.numberOfPlayers.hashCode());
		result = prime * result + ((this.expectedScore == null) ? 0 : this.expectedScore.hashCode());
		result = prime * result + ((this.gameMode == null) ? 0 : this.gameMode.hashCode());
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
		final CreateGameBody other = (CreateGameBody) obj;
		if (this.username == null) {
			if (other.username != null)
				return false;
		} else if (!this.username.equals(other.username))
			return false;
		if (this.numberOfPlayers == null) {
			if (other.numberOfPlayers != null)
				return false;
		} else if (!this.numberOfPlayers.equals(other.numberOfPlayers))
			return false;
		if (this.expectedScore == null) {
			if (other.expectedScore != null)
				return false;
		} else if (!this.expectedScore.equals(other.expectedScore))
			return false;
		if (this.gameMode == null) {
			if (other.gameMode != null)
				return false;
		} else if (!this.gameMode.equals(other.gameMode))
			return false;
		if (this.GUIID == null) {
			if (other.GUIID != null)
				return false;
		} else if (!this.GUIID.equals(other.GUIID))
			return false;
		return true;
	}
}
