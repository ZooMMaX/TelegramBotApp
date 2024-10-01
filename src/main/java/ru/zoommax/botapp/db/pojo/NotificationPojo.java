package ru.zoommax.botapp.db.pojo;

import com.mongodb.client.MongoCollection;
import lombok.Getter;
import lombok.Setter;
import ru.zoommax.HexUtils;
import ru.zoommax.MongoDBConnector;
import ru.zoommax.botapp.utils.CRC16;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class NotificationPojo extends MongoDBConnector {
    @Getter
    @Setter
    private String uid = HexUtils.toString(CRC16.calculateCRC16(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8)));
    @Getter
    @Setter
    private String tg_id;
    @Getter
    @Setter
    private String message;
    @Getter
    @Setter
    private Long date;


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
        return collection().find(eq("tg_id", this.tg_id)).into(Arrays.asList());
    }

    public List<NotificationPojo> findAll() {
        return collection().find().into(Arrays.asList());
    }

    public NotificationPojo findByUID() {
        return collection().find(eq("uid", this.uid)).first();
    }
}
