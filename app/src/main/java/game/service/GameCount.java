package game.service;

import com.fasterxml.jackson.annotation.JsonProperty;

import game.utils.Constants;

public class GameCount {
	@JsonProperty(Constants.TOTAL)
	private Long total;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.total == null) ? 0 : this.total.hashCode());
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
		final GameCount other = (GameCount) obj;
		if (this.total == null) {
			if (other.total != null)
				return false;
		} else if (!this.total.equals(other.total))
			return false;
		return true;
	}
	
}
