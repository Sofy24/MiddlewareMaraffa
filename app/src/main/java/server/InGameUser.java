package server;

import java.util.UUID;

import game.service.User;

/*
 * This class is used to store the user and the gameID of the user that is currently in a game.
 */
public record InGameUser(User user, UUID gameID) {

	public User user() {
		return this.user;
	}

	public UUID gameID() {
		return this.gameID;
	}
}
