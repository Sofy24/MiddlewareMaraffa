package server;

import java.util.UUID;

import game.service.User;

public record InGameUser(User user, UUID gameID) {

    public User user() {
        return this.user;
    }

    public UUID gameID() {
        return this.gameID;
    }
}