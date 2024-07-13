package game.service.schema;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class StateResponse {
	@JsonProperty(Constants.TRICK)
	private String trick;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StateResponse)) {
			return false;
		}
		final StateResponse that = (StateResponse) o;
		return this.trick.equals(that.trick);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.trick);
	}
}
