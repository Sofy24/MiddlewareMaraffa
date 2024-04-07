package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;
import java.util.Objects;

public class CreateGameBody {
	@JsonProperty(Constants.USERNAME)
	private String username;

	@JsonProperty(Constants.NUMBER_OF_PLAYERS)
	private Integer numberOfPlayers;

	@JsonProperty(Constants.EXPECTED_SCORE)
	private Integer expectedScore;

	@JsonProperty(Constants.GAME_MODE)
	private String gameMode;

	public Integer getExpectedScore() {
		return expectedScore;
	}

	public void setExpectedScore(final Integer expectedScore) {
		this.expectedScore = expectedScore;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public Integer getNumberOfPlayers() {
		return numberOfPlayers;
	}

	public void setNumberOfPlayers(final Integer numberOfPlayers) {
		this.numberOfPlayers = numberOfPlayers;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CreateGameBody))
			return false;
		final CreateGameBody that = (CreateGameBody) o;
		return Objects.equals(username, that.username) && Objects.equals(numberOfPlayers, that.numberOfPlayers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, numberOfPlayers);
	}
}
