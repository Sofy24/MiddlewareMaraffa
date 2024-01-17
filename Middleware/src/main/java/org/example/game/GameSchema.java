package org.example.game;

import io.vertx.core.json.JsonObject;
import org.example.user.User;

/**TODO/Define the mongodb schema of the game*/
public class GameSchema {
    private String id;
    private String firstName;
    private String lastName;
    private int score;

    // Constructors, getters, and setters

    // Convert the object to a JsonObject for MongoDB operations
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }

    // Factory method to create a Person object from a JsonObject
    public static User fromJson(JsonObject json) {
        return json.mapTo(User.class);
    }
}
