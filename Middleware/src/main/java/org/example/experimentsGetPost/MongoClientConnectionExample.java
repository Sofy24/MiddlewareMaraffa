package org.example.experimentsGetPost;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


public class MongoClientConnectionExample {
    public static void main(String[] args) throws Exception {
        String uri = "mongodb://your_mongo_user:your_mongo_password@127.0.0.1:27072";
        try {
            
            CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));


            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("MaraffaStatisticsDB").withCodecRegistry(pojoCodecRegistry);
            // MongoCollection<Document> collection = database.getCollection("MaraffaStatistics");

            // Document person = new Document()
            //         .append("firstName", "John")
            //         .append("lastName", "Doe")
            //         .append("age", 30);
            UserJoe userJoe = new UserJoe("John", "Doe", 30);

            // Insert the document into the collection
            // System.out.println(collection.find().cursor().next());
            // collection.insertOne(person);
            database.getCollection("MaraffaStatistics", UserJoe.class).insertOne(userJoe);
        } catch (Exception e){
            throw new Exception("Oh no "+e.getMessage());
        }
        /*String connectionString = "mongodb://your_mongo_user:your_mongo_password@127.0.0.1:27017/MaraffaStatisticsDB?authSource=admin";

        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();


        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();

        // Create a new client and connect to the server
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                // Send a ping to confirm a successful connection
                MongoDatabase database = mongoClient.getDatabase("MaraffaStatisticsDB");
                Document person = new Document("ping", 1);
                database.runCommand(person);
                //database.runCommand(new Document("ping", 1));
                System.out.println("Pinged your deployment. You successfully connected to MongoDB!");
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }*/
    }
}
