package game;

import java.util.ArrayList;
import java.util.List;

/** TODO/Define the mongodb schema of the game */
public class GameSchema {
	private String gameID;
	private CardSuit leadingSuit;

	public void setTrump(final CardSuit leadingSuit) {
		this.leadingSuit = leadingSuit;
	}

	public CardSuit getTrump() {
		return this.leadingSuit;
	}

	private List<Trick> tricks;

	public String getGameID() {
		return this.gameID;
	}

	public List<Trick> getTricks() {
		return this.tricks;
	}

	public GameSchema() {
	}

	public GameSchema(final String identifier, final CardSuit leadingSuit) {
		this.gameID = identifier;
		this.leadingSuit = leadingSuit;
		this.tricks = new ArrayList<>();
	}

	public void addTrick(final Trick trick) {
		this.tricks.add(trick);
	}

	@Override
	public String toString() {
		return "GameSchema [gameID=" + this.gameID + ", tricks=" + this.tricks + "]";
	}
}
