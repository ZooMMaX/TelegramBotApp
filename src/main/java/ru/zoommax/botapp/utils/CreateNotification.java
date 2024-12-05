package ru.zoommax.botapp.utils;

import lombok.extern.slf4j.Slf4j;
import ru.zoommax.botapp.db.pojo.NotificationPojo;
import ru.zoommax.botapp.db.pojo.NotificationType;

import java.nio.charset.StandardCharsets;

@Slf4j
public class CreateNotification implements Runnable{
    private final String message;
    private final String tg_id;
    private final String image;
    private final NotificationType notificationType;
    private final String fileType;
    public CreateNotification(String message, String tg_id, String image, NotificationType notificationType, String fileType) {
        if (message.getBytes(StandardCharsets.UTF_8).length > 200*2) {
            Exception ex = new Exception();
            ex.fillInStackTrace();
            throw new RuntimeException("Message length limit exceeded", ex);
        }
        this.image = image;
        this.message = message;
        this.tg_id = tg_id;
        this.notificationType = notificationType;
        this.fileType = fileType;
    }

    @Override
    public void run() {
        NotificationPojo notificationPojo = new NotificationPojo();
        notificationPojo.setMessage(message);
        notificationPojo.setTg_id(tg_id);
        notificationPojo.setFile(image);
        notificationPojo.setNotificationType(notificationType);
        notificationPojo.setFileType(fileType);
        notificationPojo.insert();
    }
}
