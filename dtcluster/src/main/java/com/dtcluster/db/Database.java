package com.dtcluster.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/** Owns the MongoDB client and named collections used by the application. */
public final class Database {
    private static MongoClient client;
    private static MongoDatabase database;

    private Database() {}

    public static synchronized void init(Properties props) {
        if (client != null) return;
        String uri = props.getProperty("mongo.uri", "mongodb://127.0.0.1:27017");
        String name = props.getProperty("mongo.database", "dtcluster");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .applyToSocketSettings(s -> s.connectTimeout(5, TimeUnit.SECONDS))
                .build();
        client = MongoClients.create(settings);
        database = client.getDatabase(name);
        // Force a quick connection check so a stopped local MongoDB is explained at startup.
        database.runCommand(new Document("ping", 1));
        createIndexes();
    }

    private static void createIndexes() {
        collection("lines").createIndex(new Document("unitId", 1));
        collection("machines").createIndex(new Document("lineId", 1));
        collection("status_logs").createIndex(new Document("machineId", 1).append("startedAt", 1));
        collection("energy_readings").createIndex(new Document("machineId", 1).append("recordedAt", 1));
    }

    public static MongoCollection<Document> collection(String name) {
        if (database == null) throw new IllegalStateException("MongoDB has not been initialized");
        return database.getCollection(name);
    }

    public static long nextId(String collectionName) {
        Document sequence = collection("sequences").findOneAndUpdate(
                new Document("_id", collectionName),
                new Document("$inc", new Document("value", 1L)),
                new com.mongodb.client.model.FindOneAndUpdateOptions().upsert(true)
                        .returnDocument(com.mongodb.client.model.ReturnDocument.AFTER));
        return sequence.getLong("value");
    }

    public static void ensureSequenceAtLeast(String name, long value) {
        collection("sequences").updateOne(new Document("_id", name),
                new Document("$max", new Document("value", value)),
                new com.mongodb.client.model.UpdateOptions().upsert(true));
    }

    public static boolean exists(String collection, Bson filter) {
        return Database.collection(collection).find(filter).limit(1).first() != null;
    }

    public static void close() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }
}
