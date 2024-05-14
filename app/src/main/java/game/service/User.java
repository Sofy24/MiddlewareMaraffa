package game.service;

import java.util.UUID;

/**
 * TODO javadoc
 */
public record User(String username, UUID clientID) {

	public String username() {
		return this.username;
	}

	public UUID clientID() {
		return this.clientID;
	}
}
