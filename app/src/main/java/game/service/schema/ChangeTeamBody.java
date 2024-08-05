package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;
import java.util.Objects;

public class ChangeTeamBody {
	@JsonProperty(Constants.GAME_ID)
	private String gameID;
	@JsonProperty(Constants.TEAM)
	private String team;
	@JsonProperty(Constants.POSITION)
	private Integer position; 
	@JsonProperty(Constants.USERNAME)
	private String username;

	

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

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
		if (!(o instanceof ChangeTeamBody)) {
			return false;
		}
			ChangeTeamBody changeTeamBody = (ChangeTeamBody) o;
		return gameID.equals(changeTeamBody.gameID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(gameID);
	}
}
