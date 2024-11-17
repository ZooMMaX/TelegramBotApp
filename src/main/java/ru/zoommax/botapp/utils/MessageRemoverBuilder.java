package ru.zoommax.botapp.utils;

import lombok.Builder;
import ru.zoommax.botapp.db.pojo.MessageRemoverPojo;

@Builder
public class MessageRemoverBuilder implements Runnable {
    private String tg_id;
    private long lastModifyViewMessage;
    private long date;
    private String message;
    private String buttonNames;
    private String buttonsCallbackData;
    @Override
    public void run() {
        MessageRemoverPojo messageRemoverPojo = new MessageRemoverPojo();
        messageRemoverPojo.setTg_id(tg_id);
        messageRemoverPojo.setLastModifyViewMessage(lastModifyViewMessage);
        messageRemoverPojo.setDate(date);
        messageRemoverPojo.setMessage(message);
        messageRemoverPojo.setButtonNames(buttonNames);
        messageRemoverPojo.setButtonsCallbackData(buttonsCallbackData);
        messageRemoverPojo.insert();
    }
}
