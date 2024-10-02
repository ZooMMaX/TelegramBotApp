package ru.zoommax.botapp.utils;

import ru.zoommax.botapp.db.pojo.NotificationPojo;

import java.nio.charset.StandardCharsets;

public class CreateNotification implements Runnable{
    private final String message;
    private final String tg_id;
    public CreateNotification(String message, String tg_id) {
        if (message.getBytes(StandardCharsets.UTF_8).length > 200*2) {
            Exception ex = new Exception();
            ex.fillInStackTrace();
            throw new RuntimeException("Message length limit exceeded", ex);
        }
        this.message = message;
        this.tg_id = tg_id;
    }

    @Override
    public void run() {
        NotificationPojo notificationPojo = new NotificationPojo();
        notificationPojo.setMessage(message);
        notificationPojo.setTg_id(tg_id);
        notificationPojo.insert();
    }
}
