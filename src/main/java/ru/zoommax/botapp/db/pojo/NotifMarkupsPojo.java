package ru.zoommax.botapp.db.pojo;

import com.mongodb.client.MongoCollection;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import lombok.Getter;
import lombok.Setter;
import ru.zoommax.MongoDBConnector;
import ru.zoommax.botapp.BotApp;
import ru.zoommax.botapp.view.Pages;

import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class NotifMarkupsPojo extends MongoDBConnector {
    @Getter
    @Setter
    private String tg_id;

    @Getter
    @Setter
    private List<Pages> pages;

    public NotifMarkupsPojo() {
    }

    @SuppressWarnings("unchecked")
    private MongoCollection<NotifMarkupsPojo> collection() {
        return (MongoCollection<NotifMarkupsPojo>) getCollection("notifMarkups", BotApp.dbName, this);
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

    public NotifMarkupsPojo find() {
        return collection().find(eq("tg_id", this.tg_id)).first();
    }
}
