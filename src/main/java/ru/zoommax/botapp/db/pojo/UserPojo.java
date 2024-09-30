package ru.zoommax.botapp.db.pojo;

import com.mongodb.client.MongoCollection;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import ru.zoommax.MongoDBConnector;

import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;


public class UserPojo extends MongoDBConnector {
    Logger logger = org.slf4j.LoggerFactory.getLogger(UserPojo.class);
    @Getter
    @Setter
    private long chatId;
    @Getter
    @Setter
    private long ViewMessageId;
    @Getter
    @Setter
    private long lastMessageId;
    @Getter
    @Setter
    private MessageType messageType;
    @Getter
    @Setter
    private List<Integer> messageIdsToDel;
    @Getter
    @Setter
    private long notificationMessageId;

    public UserPojo(){}

    @SuppressWarnings("unchecked")
    private MongoCollection<UserPojo> collection() {
        return (MongoCollection<UserPojo>) getCollection("users", "BotApp", this);
    }

    private boolean exist() {
        return collection().find(eq("chatId", this.chatId)).first() != null;
    }

    public boolean insert() {
        if (exist())
            return update();

        final String result = collection().insertOne(this).toString();
        return result.contains("insertedId");
    }

    private boolean update() {
        final String result = collection().replaceOne(eq("chatId", this.chatId), this).toString();
        return result.contains("matchedCount=1");
    }

    public boolean delete() {
        final String result = collection().deleteOne(eq("chatId", this.chatId)).toString();
        return result.contains("deletedCount=1");
    }

    public UserPojo find() {
        return collection().find(eq("chatId", this.chatId)).first();
    }
}
