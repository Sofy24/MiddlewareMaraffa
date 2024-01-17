package org.example.game;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.bson.Document;
import org.example.service.GameService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.example.user.User;
/*import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.SQLRowStream;
import io.vertx.ext.sql.SqlConnection;
import io.vertx.ext.sql.common.impl.CachedSqlClient;*/

/***This class models a game using a Verticle from vertx.
 * services = it keeps track of all the services added to the verticle
 * id = the id of the verticle
 * stateMap = it saves each state with the related trick
 * users = it keeps track of all the users added to the game*/
public class GameVerticle extends AbstractVerticle implements GameApi {

    private List<GameService> services = new ArrayList<>();
    private String id;
    private Map<String, Trick> stateMap = new ConcurrentHashMap<>();

    private List<User> users = new ArrayList<>();

    /**It starts the verticle*/
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        /*JsonObject mongoConfig = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017")
                .put("db_name", "yourMongoDB");
        // Create MongoDB client
        MongoClient mongoClient = MongoClient.createShared(vertx, mongoConfig);
        // Use the MongoDB client
        mongoClient.findOne("yourMongoCollection", new JsonObject().put("key", "value"), null, ar -> {
            if (ar.succeeded()) {
                System.out.println("MongoDB Result: " + ar.result());
            } else {
                ar.cause().printStackTrace();
            }
        });*/


        //substitute the password
        String uri = "mongodb://your_mongo_user:your_mongo_password@127.0.0.1:27017/MaraffaStatisticsDB?authSource=admin";
        try {
            MongoClient mongoClient = MongoClients.create(uri);
            MongoDatabase database = mongoClient.getDatabase("MaraffaStatisticsDB");
            MongoCollection<Document> collection = database.getCollection("MaraffaStatistics");
            System.out.println("here");
            // Create a document to insert
            Document person = new Document()
                    .append("firstName", "John")
                    .append("lastName", "Doe")
                    .append("age", 30);

            // Insert the document into the collection
            collection.insertOne(person);
            System.out.println("ciai");
            mongoClient.close();
        } catch (Exception e){
            throw new Exception("Oh no "+e.getMessage());
        }

        startPromise.complete();
    }

    @Override
    public String createGame(String username) {
        return null;
    }

    @Override
    public boolean joinGame(String username, String idGame) {
        return false;
    }

    @Override
    public void playCard(String username, String idGame, Card card) {

    }

    public List<GameService> getServices() {
        return services;
    }

    private void setServices(List<GameService> services) {
        this.services = services;
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = id;
    }

    public Map<String, Trick> getStateMap() {
        return stateMap;
    }

    private void setStateMap(Map<String, Trick> stateMap) {
        this.stateMap = stateMap;
    }
}
