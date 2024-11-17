package ru.zoommax.botapp.db.pojo;

import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import ru.zoommax.MongoDBConnector;
import ru.zoommax.botapp.BotApp;
import ru.zoommax.botapp.view.ViewMessage;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MessageRemoverPojo extends MongoDBConnector {
    @Getter
    @Setter
    private String tg_id;
    @Getter
    @Setter
    private long lastModifyViewMessage;
    @Getter
    @Setter
    private long date;
    @Getter
    @Setter
    private String message;
    @Getter
    @Setter
    private String buttonNames;
    @Getter
    @Setter
    private String buttonsCallbackData;

    public MessageRemoverPojo() {}

    @SuppressWarnings("unchecked")
    private MongoCollection<MessageRemoverPojo> collection() {
        return (MongoCollection<MessageRemoverPojo>) getCollection("removeMessages", BotApp.dbName, this);
    }

    private boolean exist() {
        return collection().find(eq("tg_id", this.tg_id)).first() != null;
    }

    public boolean insert() {
        if (exist())
            return update();

        final String result = collection().insertOne(this).toString();
        return result.contains("insertedId");
    }

    private boolean update() {
        final String result = collection().replaceOne(eq("tg_id", this.tg_id), this).toString();
        return result.contains("matchedCount=1");
    }

    public boolean delete() {
        final String result = collection().deleteOne(eq("tg_id", this.tg_id)).toString();
        return result.contains("deletedCount=1");
    }

    public MessageRemoverPojo find() {
        return collection().find(eq("tg_id", this.tg_id)).first();
    }

    public List<MessageRemoverPojo> findAll() {
        return collection().find().into(new ArrayList<>());
    }
}
