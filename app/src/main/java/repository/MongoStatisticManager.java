package repository;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import java.util.Date;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import game.GameSchema;
import game.Trick;

/*
 * A class implementing the statistic manager for mongoDB
 */
public class MongoStatisticManager extends AbstractStatisticManager {
	private MongoDatabase database;
	private final String collectionName;

	public MongoStatisticManager(final String user, final String password, final String host, final int port,
			final String collectionName) {
		final String uri = "mongodb://" + user + ":" + password + "@" + host + ":" + port;
		this.collectionName = collectionName;
		try {

			final CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
			final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
					fromProviders(pojoCodecProvider));
			// CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(),
			// fromProviders(pojoCodecProvider));

			final MongoClient mongoClient = MongoClients.create(uri);
			this.database = mongoClient.getDatabase(collectionName).withCodecRegistry(pojoCodecRegistry);
		} catch (final Exception e) {
			System.out.println("Error in MongoStatisticManager constructor: " + e.getMessage());
		}
	}

	@Override
	public void createRecord(final GameSchema schema) {
		schema.setDate(new Date());
		this.database.getCollection(this.collectionName, GameSchema.class).insertOne(schema);
	}

	@Override
	public void updateRecordWithTrick(final String recordID, final Trick trick) {
		final var res = this.database.getCollection(this.collectionName, GameSchema.class)
				.updateOne(eq("gameID", recordID), push("tricks", trick), new UpdateOptions().upsert(true));
		System.out.println("Update result: " + res);
	}

	public GameSchema getRecord(final String recordID) {
		return this.database.getCollection(this.collectionName, GameSchema.class).find(eq("gameID", recordID)).first();
	}

	public void updateSuit(final GameSchema gameSchema) {
		this.database.getCollection(this.collectionName, GameSchema.class).updateOne(
				eq("gameID", gameSchema.getGameID()), set("leadingSuit", gameSchema.getTrump()),
				new UpdateOptions().upsert(true));
	}
}
