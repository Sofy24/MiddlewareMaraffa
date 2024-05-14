package game.service.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import game.utils.Constants;
import java.util.Objects;

public class IsRoundEndedResponse {
	@JsonProperty(Constants.ENDED)
	private Boolean isEnded;

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof IsRoundEndedResponse))
			return false;
		final IsRoundEndedResponse that = (IsRoundEndedResponse) o;
		return isEnded == that.isEnded;
	}

	@Override
	public int hashCode() {
		return Objects.hash(isEnded);
	}
}
