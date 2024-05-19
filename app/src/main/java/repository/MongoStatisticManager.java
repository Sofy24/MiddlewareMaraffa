package repository;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import game.GameSchema;
import game.Trick;

// TODO ma un bel singleton?
public class MongoStatisticManager extends AbstractStatisticManager {
	private MongoDatabase database;

	// TODO andranno passati a costruttore tanti parametri quanti sono i parametri
	// di connessione
	public MongoStatisticManager() {
		final String uri = "mongodb://your_mongo_user:your_mongo_password@127.0.0.1:27012";
		try {

			final CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
			final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
					fromProviders(pojoCodecProvider));
			// CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
			// fromProviders(pojoCodecProvider));

			final MongoClient mongoClient = MongoClients.create(uri);
			this.database = mongoClient.getDatabase("MaraffaStatisticsDB-test").withCodecRegistry(pojoCodecRegistry);
		} catch (final Exception e) {
			System.out.println("Error in MongoStatisticManager constructor: " + e.getMessage());
		}
	}

	@Override
	public void createRecord(final GameSchema schema) {
		this.database.getCollection("MaraffaStatistics", GameSchema.class).insertOne(schema);
	}

	@Override
	public void updateRecordWithTrick(final String recordID, final Trick trick) {
		final var res = this.database.getCollection("MaraffaStatistics", GameSchema.class)
				.updateOne(eq("gameID", recordID), push("tricks", trick), new UpdateOptions().upsert(true));
		System.out.println("Update result: " + res);
	}

	public GameSchema getRecord(final String recordID) {
		return this.database.getCollection("MaraffaStatistics", GameSchema.class).find(eq("gameID", recordID)).first();
	}

	public void updateSuit(final GameSchema gameSchema) {
		this.database.getCollection("MaraffaStatistics", GameSchema.class).updateOne(
				eq("gameID", gameSchema.getGameID()), set("leadingSuit", gameSchema.getTrump()),
				new UpdateOptions().upsert(true));
	}
}
