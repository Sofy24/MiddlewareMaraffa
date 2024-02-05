package org.example.experimentsGetPost;

import io.vertx.core.json.JsonObject;

public class UserJoe {
    private String firstName;

    private String lastName;

    private int age;

    public UserJoe() {}

    public UserJoe(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }

    // public JsonObject toJson() {
    //     return JsonObject.mapFrom(this);
    // }
}
