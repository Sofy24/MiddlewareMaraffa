package game;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** TODO/Define the mongodb schema of the game */
public class GameSchema {
	private String gameID;
	private CardSuit leadingSuit;
	private Date date;

	public Date getDate() {
		return this.date;
	}

	public void setDate(final Date date) {
		this.date = date;
	}

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
		this.date = new Date();
	}

	public void addTrick(final Trick trick) {
		this.tricks.add(trick);
	}

	@Override
	public String toString() {
		return "GameSchema [gameID=" + this.gameID + ", tricks=" + this.tricks + "]";
	}
}
