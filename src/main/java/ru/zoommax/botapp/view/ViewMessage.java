package ru.zoommax.botapp.view;

import com.pengrad.telegrambot.model.request.InputMediaAudio;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.model.request.InputMediaVideo;
import com.pengrad.telegrambot.request.*;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zoommax.botapp.db.pojo.MessageType;
import ru.zoommax.botapp.db.pojo.UserPojo;

import java.io.File;

import static ru.zoommax.botapp.BotApp.bot;

@Builder
public class ViewMessage implements Runnable{

    private final String message;
    private final long chatId;
    private KeyboardMarkup callbackKeyboard;
    private File image;
    private File video;
    private File audio;
    private String caption;

    @Override
    public void run() {
        Logger logger = LoggerFactory.getLogger(ViewMessage.class);
        UserPojo userPojo = new UserPojo();
        userPojo.setChatId(chatId);
        userPojo = userPojo.find();
        long ViewMessageId = userPojo.getViewMessageId();
        long messageId = userPojo.getLastMessageId();
        MessageType messageType = userPojo.getMessageType();
        if (messageId > -1) {
            bot.execute(new DeleteMessage(chatId, Math.toIntExact(messageId)));
        }

        if (messageType == MessageType.TEXT) {
            if (image != null || video != null || audio != null) {
                sendMedia(chatId);
                return;
            }
            if (message != null) {
                bot.execute(new EditMessageText(chatId, Math.toIntExact(ViewMessageId), message));
            }

            if (callbackKeyboard != null) {
                EditMessageReplyMarkup e = new EditMessageReplyMarkup(chatId, Math.toIntExact(ViewMessageId));
                e.replyMarkup(callbackKeyboard.getInlineKeyboard());
                bot.execute(e);
            }
        }
        if (messageType == MessageType.MEDIA){
            if (message != null) {
                sendText(chatId);
                return;
            }
            if (image != null && video != null && audio != null || image != null && video != null || image != null && audio != null || video != null && audio != null) {
                logger.error("You can't send image, video and audio at the same time");
                return;
            }

            if (image != null) {
                bot.execute(new EditMessageMedia(chatId, Math.toIntExact(ViewMessageId), new InputMediaPhoto(image)));
            }


            if (video != null) {
                bot.execute(new EditMessageMedia(chatId, Math.toIntExact(ViewMessageId), new InputMediaVideo(video)));
            }

            if (audio != null) {
                bot.execute(new EditMessageMedia(chatId, Math.toIntExact(ViewMessageId), new InputMediaAudio(audio)));
            }
        }
    }

    private void sendMedia(long chatId) {
        if (caption == null){
            caption = "";
        }
        UserPojo userPojo = new UserPojo();
        userPojo.setChatId(chatId);
        userPojo = userPojo.find();
        int viewMessageId = Math.toIntExact(userPojo.getViewMessageId());
        Logger logger = LoggerFactory.getLogger(ViewMessage.class);
        bot.execute(new DeleteMessage(chatId, Math.toIntExact(viewMessageId)));
        if (image != null && video != null && audio != null || image != null && video != null || image != null && audio != null || video != null && audio != null) {
            logger.error("You can't send image, video and audio at the same time");
            return;
        }

        if (image != null) {
            SendPhoto sendPhoto = new SendPhoto(chatId, image).caption(caption);
            if (callbackKeyboard != null) {
                sendPhoto.replyMarkup(callbackKeyboard.getInlineKeyboard());
            }
            viewMessageId = bot.execute(sendPhoto).message().messageId();
        }

        if (video != null) {
            SendVideo sendVideo = new SendVideo(chatId, video).caption(caption);
            if (callbackKeyboard != null) {
                sendVideo.replyMarkup(callbackKeyboard.getInlineKeyboard());
            }
            viewMessageId = bot.execute(sendVideo).message().messageId();
        }

        if (audio != null) {
            SendAudio sendAudio = new SendAudio(chatId, audio).caption(caption);
            if (callbackKeyboard != null) {
                sendAudio.replyMarkup(callbackKeyboard.getInlineKeyboard());
            }
            viewMessageId = bot.execute(sendAudio).message().messageId();
        }

        userPojo.setViewMessageId(viewMessageId);
        userPojo.setMessageType(MessageType.MEDIA);
        userPojo.insert();
    }

    private void sendText(long chatId) {
        UserPojo userPojo = new UserPojo();
        userPojo.setChatId(chatId);
        userPojo = userPojo.find();
        int viewMessageId = Math.toIntExact(userPojo.getViewMessageId());
        bot.execute(new DeleteMessage(chatId, Math.toIntExact(viewMessageId)));
        SendMessage sendMessage = new SendMessage(chatId, message);
        if (callbackKeyboard != null) {
            sendMessage.replyMarkup(callbackKeyboard.getInlineKeyboard());
        }
        viewMessageId = bot.execute(sendMessage).message().messageId();
        userPojo.setViewMessageId(viewMessageId);
        userPojo.setMessageType(MessageType.TEXT);
        userPojo.insert();
    }
}
