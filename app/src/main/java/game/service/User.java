package game.service;

import java.util.UUID;

public record User(String username, UUID clientID) {

	public String username() {
		return this.username;
	}

	public UUID clientID() {
		return this.clientID;
	}
}
