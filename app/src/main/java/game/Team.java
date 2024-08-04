package game;

import java.util.List;

import game.service.User;

/** A record modelling the concept of "team" */
public record Team(List<User> players, String nameOfTeam, Integer score, Integer currentScore) {

	@Override
	public List<User> players() {
		return this.players;
	}

	@Override
	public String nameOfTeam() {
		return this.nameOfTeam;
	}

	@Override
	public Integer score() {
		return this.score;
	}

	@Override
	public Integer currentScore() {
		return this.currentScore;
	}

	@Override
	public String toString() {
		return "Team{" + "players=" + this.players + ", nameOfTeam='" + this.nameOfTeam + '\'' + ", score='"
				+ this.score + '\''
				+ ", currentScore='"
				+ this.currentScore+ '\'' +
				+ '}';
	}
}
