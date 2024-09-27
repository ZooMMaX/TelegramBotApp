package ru.zoommax.botapp;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import ru.zoommax.botapp.db.pojo.MessageType;
import ru.zoommax.botapp.db.pojo.UserMarkupsPojo;
import ru.zoommax.botapp.db.pojo.UserPojo;
import ru.zoommax.botapp.view.KBUnsafe;
import ru.zoommax.botapp.view.KeyboardMarkup;
import ru.zoommax.botapp.view.Pages;
import ru.zoommax.botapp.view.ViewMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotApp implements Runnable {

    public static TelegramBot bot;
    public static int ButtonsRows = 4;
    private final Listener listener;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public BotApp(String token, Listener listener) {
        bot = new TelegramBot(token);
        this.listener = listener;
    }

    public BotApp(String token, Listener listener, int ButtonsRows) {
        bot = new TelegramBot(token);
        this.listener = listener;
        BotApp.ButtonsRows = ButtonsRows;
    }
    @Override
    public void run() {
        Logger logger = org.slf4j.LoggerFactory.getLogger(BotApp.class);
        bot.setUpdatesListener(updates -> {
            // ... process updates
            // return id of last processed update or confirm them all
            for (Update update : updates) {
                UserPojo userPojo = new UserPojo();
                long chatId = 0;
                if (update.message() != null) {
                    chatId = update.message().chat().id();
                }else if (update.callbackQuery() != null) {
                    chatId = update.callbackQuery().from().id();
                } else if (update.inlineQuery() != null) {
                    chatId = update.inlineQuery().from().id();
                } else if (update.chosenInlineResult() != null) {
                    chatId = update.chosenInlineResult().from().id();
                }

                long lastMessageId = 0;
                if (update.message() != null) {
                    lastMessageId = update.message().messageId();
                }else if (update.callbackQuery() != null) {
                    lastMessageId = update.callbackQuery().message().messageId();
                }

                userPojo.setChatId(chatId);
                userPojo = userPojo.find();
                if (userPojo == null) {
                    userPojo = new UserPojo();
                    userPojo.setChatId(chatId);
                    userPojo.setLastMessageId(lastMessageId);
                    try {
                        userPojo.setViewMessageId(bot.execute(new SendMessage(chatId, "Bot starting...")).message().messageId());
                    }catch (Exception e){
                        logger.error(Arrays.toString(e.getStackTrace()));
                    }
                    userPojo.setMessageType(MessageType.TEXT);
                    userPojo.insert();
                }else {
                    if (update.callbackQuery() != null){
                        userPojo.setLastMessageId(-1);
                        userPojo.insert();
                    }else {
                        userPojo.setLastMessageId(lastMessageId);
                        userPojo.insert();
                    }
                }


                ViewMessage viewMessage = null;
                if (update.message() != null) {
                    if (update.message().photo() != null) {
                        viewMessage = listener.onPicture(update.message().photo(), update.message().caption(), update.message().messageId(), update.message().chat().id(), update);
                    }else if (update.message().text() != null){
                        if(update.message().text().startsWith("/")) {
                            if (update.message().text().equals("/start")) {
                                long msgId = 0;
                                try {
                                    msgId = bot.execute(new SendMessage(update.message().chat().id(), "Bot starting...")).message().messageId();
                                }catch (NullPointerException e){
                                    e.printStackTrace();
                                    msgId = 0;
                                }
                                userPojo.setViewMessageId(msgId);
                                userPojo.insert();
                            }
                            viewMessage = listener.onCommand(update.message().text(), update.message().messageId(), update.message().chat().id(), update);
                        }else {
                            viewMessage = listener.onMessage(update.message().text(), update.message().messageId(), update.message().chat().id(), update);
                        }
                    }
                }

                if (update.callbackQuery() != null) {
                    if (update.callbackQuery().data().contains("nextButton")){
                        int keysPage = Integer.parseInt(update.callbackQuery().data().split(":")[1])+1;
                        UserMarkupsPojo userMarkupsPojo = new UserMarkupsPojo();
                        userMarkupsPojo.setTg_id(chatId+"");
                        userMarkupsPojo = userMarkupsPojo.find();

                        List<Pages> pages = userMarkupsPojo.getPages();
                        Pages page = pages.get(keysPage);

                        KBUnsafe keyboardMarkup = KBUnsafe.builder()
                                .chatId(chatId)
                                .buttonsNames(page.getButtonsNames())
                                .buttonsCallbackData(page.getButtonsCallbacksData())
                                .build();

                        viewMessage = ViewMessage.builder()
                                .chatId(chatId)
                                .kbUnsafe(keyboardMarkup)
                                .build();
                    } else if (update.callbackQuery().data().contains("prevButton")){
                        int keysPage = Integer.parseInt(update.callbackQuery().data().split(":")[1])-1;
                        UserMarkupsPojo userMarkupsPojo = new UserMarkupsPojo();
                        userMarkupsPojo.setTg_id(chatId+"");
                        userMarkupsPojo = userMarkupsPojo.find();

                        List<Pages> pages = userMarkupsPojo.getPages();
                        Pages page = pages.get(keysPage);

                        KBUnsafe keyboardMarkup = KBUnsafe.builder()
                                .chatId(chatId)
                                .buttonsNames(page.getButtonsNames())
                                .buttonsCallbackData(page.getButtonsCallbacksData())
                                .build();

                        viewMessage = ViewMessage.builder()
                                .chatId(chatId)
                                .kbUnsafe(keyboardMarkup)
                                .build();

                    } else {
                        viewMessage = listener.onCallbackQuery(update.callbackQuery().data(), update.callbackQuery().message().messageId(), update.callbackQuery().message().chat().id(), update);
                    }
                }
                if (update.inlineQuery() != null) {
                    viewMessage = listener.onInlineQuery(update.inlineQuery().query(), update.inlineQuery().id(), update.inlineQuery().from().id(), update);
                }
                if (update.chosenInlineResult() != null) {
                    viewMessage = listener.onChosenInlineResult(update.chosenInlineResult().resultId(), update.chosenInlineResult().from().id(), update.chosenInlineResult().query(), update);
                }
                if (viewMessage == null) {
                    return UpdatesListener.CONFIRMED_UPDATES_ALL;
                }
                executor.submit(viewMessage);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
// Create Exception Handler
        }, e -> {
            if (e.response() != null) {
                // got bad response from telegram
                e.response().errorCode();
                e.response().description();
            } else {
                // probably network error
                e.printStackTrace();
            }
        });
    }
}
