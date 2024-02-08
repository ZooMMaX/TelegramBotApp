package ru.zoommax.botapp.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DBConnector {

    private MongoClient mongoClient = null;
    public Object getCollection(String name, Class cls){
        CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();
        CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

        String uri = "mongodb://127.0.0.1:27017/?retryWrites=true&w=majority";
        mongoClient = MongoClients.create(uri);
        MongoDatabase database = mongoClient.getDatabase("BotApp").withCodecRegistry(pojoCodecRegistry);
        return database.getCollection(name, cls);
    }

    public void close(){
        mongoClient.close();
    }
}