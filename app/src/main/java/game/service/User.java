package game.service;

import java.util.UUID;

/**
 * TODO javadoc
 */
public record User(String username, UUID clientID, boolean guest) {

	public String username() {
		return this.username;
	}

	public UUID clientID() {
		return this.clientID;
	}

	public boolean guest() {
		return this.guest;
	}
}
