package ru.zoommax.botapp.view;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zoommax.botapp.db.pojo.MessageType;
import ru.zoommax.botapp.db.pojo.UserPojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.zoommax.botapp.BotApp.bot;

@Builder
public class ViewMessage implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(ViewMessage.class);
    private String message;
    private final long chatId;
    private KeyboardMarkup callbackKeyboard;
    private File image;
    private List<File> images;
    private File video;
    private File audio;
    private File document;
    private String caption;
    private boolean captionAsMessage = false;

    @Override
    public void run() {
        Logger logger = LoggerFactory.getLogger(ViewMessage.class);
        if (caption != null && caption.length() > 1024) {
            message = caption;
            caption = null;
            captionAsMessage = true;
            images = new ArrayList<>();
            images.add(image);
            image = null;
        }
        UserPojo userPojo = new UserPojo();
        userPojo.setChatId(chatId);
        userPojo = userPojo.find();

        List<Integer> messageIdsToDel = userPojo.getMessageIdsToDel();
        if ((message != null && !captionAsMessage) || image != null || video != null || audio != null || images != null || document != null) {
            if (messageIdsToDel != null) {
                for (int messageId : messageIdsToDel) {
                    bot.execute(new DeleteMessage(chatId, messageId));
                }
            }
        }

        long ViewMessageId = userPojo.getViewMessageId();
        long messageId = userPojo.getLastMessageId();
        MessageType messageType = userPojo.getMessageType();
        if (messageId > -1) {
            bot.execute(new DeleteMessage(chatId, Math.toIntExact(messageId)));
        }

        if (image == null && video == null && audio == null && message == null && caption == null && images == null && document == null) {
            EditMessageReplyMarkup e = new EditMessageReplyMarkup(chatId, Math.toIntExact(ViewMessageId));
            e.replyMarkup(callbackKeyboard.getInlineKeyboard());
            bot.execute(e);
        } else {

            if (messageType == MessageType.TEXT) {
                if (image != null || video != null || audio != null || images != null || document != null) {
                    sendMedia(chatId);
                    return;
                }

                bot.execute(new EditMessageText(chatId, Math.toIntExact(ViewMessageId), message).parseMode(ParseMode.HTML));


                if (callbackKeyboard != null) {
                    EditMessageReplyMarkup e = new EditMessageReplyMarkup(chatId, Math.toIntExact(ViewMessageId));
                    e.replyMarkup(callbackKeyboard.getInlineKeyboard());
                    bot.execute(e);
                }
            }
            if (messageType == MessageType.MEDIA) {
                if (message != null) {
                    sendText(chatId);
                    return;
                }

                if (image != null && video != null && audio != null && images != null && document != null ||
                        image != null && video != null || image != null && audio != null || image != null && images != null ||
                        image != null && document != null || video != null && audio != null && images != null && document != null ||
                        video != null && audio != null && images != null || video != null && audio != null && document != null ||
                        video != null && images != null && document != null || audio != null && images != null && document != null) {
                    logger.error("You can't send image, video and audio at the same time");
                    return;
                }

                if (image != null) {
                    bot.execute(new EditMessageMedia(chatId, Math.toIntExact(ViewMessageId), new InputMediaPhoto(image)));
                    if (caption != null) {
                        EditMessageCaption editMessageCaption = new EditMessageCaption(chatId, Math.toIntExact(ViewMessageId));
                        editMessageCaption.caption(caption).parseMode(ParseMode.HTML);
                        bot.execute(editMessageCaption);
                    }
                    if (callbackKeyboard != null) {
                        EditMessageReplyMarkup e = new EditMessageReplyMarkup(chatId, Math.toIntExact(ViewMessageId));
                        e.replyMarkup(callbackKeyboard.getInlineKeyboard());
                        bot.execute(e);
                    }
                }


                if (video != null) {
                    bot.execute(new EditMessageMedia(chatId, Math.toIntExact(ViewMessageId), new InputMediaVideo(video)));
                    if (caption != null) {
                        EditMessageCaption editMessageCaption = new EditMessageCaption(chatId, Math.toIntExact(ViewMessageId));
                        editMessageCaption.caption(caption).parseMode(ParseMode.HTML);
                        bot.execute(editMessageCaption);
                    }
                    if (callbackKeyboard != null) {
                        EditMessageReplyMarkup e = new EditMessageReplyMarkup(chatId, Math.toIntExact(ViewMessageId));
                        e.replyMarkup(callbackKeyboard.getInlineKeyboard());
                        bot.execute(e);
                    }
                }

                if (audio != null) {
                    bot.execute(new EditMessageMedia(chatId, Math.toIntExact(ViewMessageId), new InputMediaAudio(audio)));
                    if (caption != null) {
                        EditMessageCaption editMessageCaption = new EditMessageCaption(chatId, Math.toIntExact(ViewMessageId));
                        editMessageCaption.caption(caption).parseMode(ParseMode.HTML);
                        bot.execute(editMessageCaption);
                    }
                    if (callbackKeyboard != null) {
                        EditMessageReplyMarkup e = new EditMessageReplyMarkup(chatId, Math.toIntExact(ViewMessageId));
                        e.replyMarkup(callbackKeyboard.getInlineKeyboard());
                        bot.execute(e);
                    }
                }

                if (images != null) {
                    sendMedia(chatId);
                }

                if (document != null) {
                    bot.execute(new EditMessageMedia(chatId, Math.toIntExact(ViewMessageId), new InputMediaDocument(document)));
                    if (caption != null) {
                        EditMessageCaption editMessageCaption = new EditMessageCaption(chatId, Math.toIntExact(ViewMessageId));
                        editMessageCaption.caption(caption).parseMode(ParseMode.HTML);
                        bot.execute(editMessageCaption);
                    }
                    if (callbackKeyboard != null) {
                        EditMessageReplyMarkup e = new EditMessageReplyMarkup(chatId, Math.toIntExact(ViewMessageId));
                        e.replyMarkup(callbackKeyboard.getInlineKeyboard());
                        bot.execute(e);
                    }
                }
            }
        }
    }

    private void sendMedia(long chatId) {
        if (caption == null && message == null){
            caption = "";
        }
        UserPojo userPojo = new UserPojo();
        userPojo.setChatId(chatId);
        userPojo = userPojo.find();
        int viewMessageId = Math.toIntExact(userPojo.getViewMessageId());
        Logger logger = LoggerFactory.getLogger(ViewMessage.class);
        bot.execute(new DeleteMessage(chatId, Math.toIntExact(viewMessageId)));
        if (image != null && video != null && audio != null && images != null && document != null ||
                image != null && video != null || image != null && audio != null || image != null && images != null ||
                image != null && document != null || video != null && audio != null && images != null && document != null ||
                video != null && audio != null && images != null || video != null && audio != null && document != null ||
                video != null && images != null && document != null || audio != null && images != null && document != null) {
            logger.error("You can't send image, video and audio at the same time");
            return;
        }

        if (image != null) {
            SendPhoto sendPhoto = new SendPhoto(chatId, image).caption(caption).parseMode(ParseMode.HTML);
            if (callbackKeyboard != null) {
                sendPhoto.replyMarkup(callbackKeyboard.getInlineKeyboard());
            }
            viewMessageId = bot.execute(sendPhoto).message().messageId();
        }

        if (video != null) {
            SendVideo sendVideo = new SendVideo(chatId, video).caption(caption).parseMode(ParseMode.HTML);
            if (callbackKeyboard != null) {
                sendVideo.replyMarkup(callbackKeyboard.getInlineKeyboard());
            }
            viewMessageId = bot.execute(sendVideo).message().messageId();
        }

        if (audio != null) {
            SendAudio sendAudio = new SendAudio(chatId, audio).caption(caption).parseMode(ParseMode.HTML);
            if (callbackKeyboard != null) {
                sendAudio.replyMarkup(callbackKeyboard.getInlineKeyboard());
            }
            viewMessageId = bot.execute(sendAudio).message().messageId();
        }

        if (images != null) {
            InputMediaPhoto[] imageAlbum = new InputMediaPhoto[images.size()];
            for (int i = 0; i < images.size(); i++) {
                imageAlbum[i] = new InputMediaPhoto(images.get(i));
            }
            SendMediaGroup sendMediaGroup = new SendMediaGroup(chatId, imageAlbum);
            Message[] messages = bot.execute(sendMediaGroup).messages();
            List<Integer> messageIds = new ArrayList<>();
            for (Message value : messages) {
                messageIds.add(value.messageId());
            }
            userPojo.setMessageIdsToDel(messageIds);
            userPojo.insert();
            if (callbackKeyboard != null) {
                messageIds.add(messages[messageIds.size()-1].messageId()+1);
                if ((message == null || message.isEmpty()) && caption == null) {
                    message = ".";
                } else if ((message == null || message.isEmpty())) {
                    message = caption;
                }
                sendText(chatId);
                return;
            }else {
                viewMessageId = messageIds.get(messageIds.size()-1);
            }
            if (caption != null && !caption.isEmpty() && caption.length()<=1024) {
                EditMessageCaption editMessageCaption = new EditMessageCaption(chatId, messageIds.get(messageIds.size()-1));
                editMessageCaption.caption(caption).parseMode(ParseMode.HTML);
                bot.execute(editMessageCaption);
            }
        }

        if (document != null) {
            SendDocument sendDocument = new SendDocument(chatId, document).caption(caption).parseMode(ParseMode.HTML);
            if (callbackKeyboard != null) {
                sendDocument.replyMarkup(callbackKeyboard.getInlineKeyboard());
            }
            viewMessageId = bot.execute(sendDocument).message().messageId();
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
        SendMessage sendMessage = new SendMessage(chatId, message).parseMode(ParseMode.HTML);
        if (callbackKeyboard != null) {
            sendMessage.replyMarkup(callbackKeyboard.getInlineKeyboard());
        }
        viewMessageId = bot.execute(sendMessage).message().messageId();
        userPojo.setViewMessageId(viewMessageId);
        userPojo.setMessageType(MessageType.TEXT);
        userPojo.insert();
    }
}
