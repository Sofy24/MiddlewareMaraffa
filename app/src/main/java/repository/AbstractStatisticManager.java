package repository;

import game.GameSchema;
import game.Trick;

public abstract class AbstractStatisticManager {
	// TODO matte queste chiamate devo essere sparate e non attesa, o sarebbe comodo
	// che ogni verticle
	// si connetta al db e faccia le sue cose ???
	public abstract void createRecord(GameSchema schema);

	public abstract void updateRecordWithTrick(String recordID, Trick trick);

	public abstract void updateSuit(GameSchema gameSchema);

	// TODO se aggiungo metodo alle classe ereditate non le vedono perche non le ho
	// messe qui,
	// sicuramente va vista la questione e sicuramente si devono usare i generics
	// per (in questo caso)
	// GameSchema
}
