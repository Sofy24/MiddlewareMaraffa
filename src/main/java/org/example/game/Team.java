package org.example.game;

import java.util.List;

/**
 * A record modelling the concept of "team"
 */
public record Team(List<String> players, String nameOfTeam) {

    @Override
    public List<String> players() {
        return players;
    }

    @Override
    public String nameOfTeam() {
        return nameOfTeam;
    }

    @Override
    public String toString() {
        return "Team{" +
                "players=" + players +
                ", nameOfTeam='" + nameOfTeam + '\'' +
                '}';
    }
}
