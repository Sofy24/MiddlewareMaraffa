package org.example.repository;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.example.game.GameSchema;
import org.example.game.Trick;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


//TODO ma un bel singleton?
public class MongoStatisticManager extends AbstractStatisticManager {
    private MongoDatabase database;

    //TODO andranno passati a costruttore tanti parametri quanti sono i parametri di connessione
    public MongoStatisticManager() {
        String uri = "mongodb://your_mongo_user:your_mongo_password@127.0.0.1:27012";
        try {
            
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

            MongoClient mongoClient = MongoClients.create(uri);
            this.database = mongoClient.getDatabase("MaraffaStatisticsDB").withCodecRegistry(pojoCodecRegistry);
        } catch (Exception e){
            System.out.println("Error in MongoStatisticManager constructor: " + e.getMessage());
        }
    }

    @Override
    public void createRecord(GameSchema schema) {
        this.database.getCollection("MaraffaStatistics", GameSchema.class).insertOne(schema);
    }

    @Override
    public void updateRecordWithTrick(String recordID, Trick trick) {
        throw new UnsupportedOperationException("Unimplemented method 'updateRecordWithTrick'");
    }


    public GameSchema getRecord(String recordID) {
        return null;
        // return this.database.getCollection("MaraffaStatistics", GameSchema.class).find(eq("gameID", recordID)).first();
    }
}
