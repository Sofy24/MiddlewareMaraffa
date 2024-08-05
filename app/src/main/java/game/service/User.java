package game.service;

import java.util.UUID;

/*
 	Java record class named `User` in the `game.service` package. The
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
