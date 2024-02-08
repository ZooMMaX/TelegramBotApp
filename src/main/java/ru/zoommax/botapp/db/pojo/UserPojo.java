package ru.zoommax.botapp.db.pojo;

import com.mongodb.client.MongoCollection;
import lombok.Data;
import ru.zoommax.botapp.db.DBConnector;

import static com.mongodb.client.model.Filters.eq;

@Data
public class UserPojo extends DBConnector {
    private long chatId;
    private long ViewMessageId;
    private long lastMessageId;
    private MessageType messageType;

    public UserPojo(){}

    @SuppressWarnings("unchecked")
    private MongoCollection<UserPojo> collection() {
        return (MongoCollection<UserPojo>) getCollection("users", UserPojo.class);
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
        final UserPojo userPojo = collection().find(eq("chatId", this.chatId)).first();
        return userPojo;
    }
}
