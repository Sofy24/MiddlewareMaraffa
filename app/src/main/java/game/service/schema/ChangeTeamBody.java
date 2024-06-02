package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class  ChangeTeamBody {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;

	@JsonProperty(Constants.USERNAME)
	private String username;

	@JsonProperty(Constants.TEAM)
	private String team;

	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((gameID == null) ? 0 : gameID.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((team == null) ? 0 : team.hashCode());
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
		ChangeTeamBody other = (ChangeTeamBody) obj;
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
		if (team == null) {
			if (other.team != null)
				return false;
		} else if (!team.equals(other.team))
			return false;
		return true;
	}

	
}
