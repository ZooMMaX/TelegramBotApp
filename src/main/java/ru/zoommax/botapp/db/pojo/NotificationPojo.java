package ru.zoommax.botapp.db.pojo;

import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import ru.zoommax.MongoDBConnector;
import ru.zoommax.botapp.utils.CRC16;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;

import static com.mongodb.client.model.Filters.eq;

public class NotificationPojo extends MongoDBConnector {
    @Getter
    @Setter
    private String uid = guid();
    @Getter
    @Setter
    private String tg_id;
    @Getter
    @Setter
    private String message;
    @Getter
    @Setter
    private Long date = System.currentTimeMillis();


    private String guid() {
        CRC32 crc32 = new CRC32();
        crc32.update(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));
        String result = Long.toHexString(crc32.getValue());
        crc32.reset();
        return result;
    }

    public NotificationPojo() {
    }

    @SuppressWarnings("unchecked")
    private MongoCollection<NotificationPojo> collection() {
        return (MongoCollection<NotificationPojo>) getCollection("notifications", "BotApp", this);
    }

    private boolean exist() {
        return collection().find(eq("uid", this.uid)).first() != null;
    }

    public boolean insert() {
        if (exist())
            return update();

        final String result = collection().insertOne(this).toString();
        return result.contains("insertedId");
    }

    private boolean update() {
        final String result = collection().replaceOne(eq("uid", this.uid), this).toString();
        return result.contains("matchedCount=1");
    }

    public boolean delete() {
        final String result = collection().deleteOne(eq("uid", this.uid)).toString();
        return result.contains("deletedCount=1");
    }

    public NotificationPojo find() {
        return collection().find(eq("uid", this.uid)).first();
    }

    public List<NotificationPojo> findAllByTgId() {
        return collection().find(eq("tg_id", this.tg_id)).into(new ArrayList<>());
    }

    public List<NotificationPojo> findAll() {
        return collection().find().into(new ArrayList<>());
    }

    public NotificationPojo findByUID() {
        return collection().find(eq("uid", this.uid)).first();
    }
}
