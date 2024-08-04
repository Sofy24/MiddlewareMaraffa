package repository;

import game.GameSchema;
import game.Trick;

/*
 * An abstract class with the methods of the statistic manager for a generic database
 */
public abstract class AbstractStatisticManager {
	public abstract void createRecord(GameSchema schema);

	public abstract void updateRecordWithTrick(String recordID, Trick trick);

	public abstract void updateSuit(GameSchema gameSchema);

	public abstract long getGamesCompleted();
}
