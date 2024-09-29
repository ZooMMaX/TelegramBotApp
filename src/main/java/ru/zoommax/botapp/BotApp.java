package ru.zoommax.botapp;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zoommax.botapp.db.pojo.MessageType;
import ru.zoommax.botapp.db.pojo.UserMarkupsPojo;
import ru.zoommax.botapp.db.pojo.UserPojo;
import ru.zoommax.botapp.utils.VML;
import ru.zoommax.botapp.utils.ViewMessageListener;
import ru.zoommax.botapp.view.KBUnsafe;
import ru.zoommax.botapp.view.Pages;
import ru.zoommax.botapp.view.ViewMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotApp implements Runnable {
    public static String defaultErrorMessage;
    public static TelegramBot bot;
    public static int ButtonsRows = 4;
    public static HashMap<String, String> nextBtn = new HashMap<>();//"➡️";
    public static HashMap<String, String> prevBtn = new HashMap<>();//"⬅️";
    private Listener listener;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(Thread.currentThread().getStackTrace()[2].toString().split("\\.")[0]))
                .setScanners(Scanners.SubTypes, Scanners.ConstructorsAnnotated, Scanners.MethodsAnnotated, Scanners.FieldsAnnotated, Scanners.TypesAnnotated));
    Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(ViewMessageListener.class);

    public BotApp(String token, Listener listener) {
        nextBtn.put("default", "➡️");
        prevBtn.put("default", "⬅️");
        defaultErrorMessage = "Error. Please send /start";
        bot = new TelegramBot(token);
        this.listener = listener;
    }

    public BotApp(String token, Listener listener, int ButtonsRows) {
        nextBtn.put("default", "➡️");
        prevBtn.put("default", "⬅️");
        defaultErrorMessage = "Error. Please send /start";
        bot = new TelegramBot(token);
        this.listener = listener;
        BotApp.ButtonsRows = ButtonsRows;
    }

    public BotApp(String token) {
        nextBtn.put("default", "➡️");
        prevBtn.put("default", "⬅️");
        defaultErrorMessage = "Error. Please send /start";
        bot = new TelegramBot(token);
    }

    public BotApp(String token, int ButtonsRows) {
        nextBtn.put("default", "➡️");
        prevBtn.put("default", "⬅️");
        defaultErrorMessage = "Error. Please send /start";
        bot = new TelegramBot(token);
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
                        if (listener != null) {
                            viewMessage = listener.onPicture(update.message().photo(), update.message().caption(), update.message().messageId(), update.message().chat().id(), update);
                        }
                        if (annotated != null) {
                            for (Class<?> listener : annotated) {
                                //viewMessage = listener.onPicture(update.message().photo(), update.message().caption(), update.message().messageId(), update.message().chat().id(), update);
                                try {
                                    Method method = listener.getMethod("onPicture", PhotoSize[].class, String.class, int.class, long.class, Update.class);
                                    viewMessage = (ViewMessage) method.invoke(listener.getDeclaredConstructor().newInstance(), update.message().photo(), update.message().caption(), update.message().messageId(), update.message().chat().id(), update);
                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                         NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    } else if (update.message().text() != null) {
                        if (update.message().text().startsWith("/")) {
                            if (update.message().text().equals("/start")) {
                                long msgId = 0;
                                try {
                                    msgId = bot.execute(new SendMessage(update.message().chat().id(), "Bot starting...")).message().messageId();
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                    msgId = 0;
                                }
                                userPojo.setViewMessageId(msgId);
                                userPojo.insert();
                            }
                            if (annotated != null) {
                                for (Class<?> listener : annotated) {
                                    //viewMessage = listener.onCommand(update.message().text(), update.message().messageId(), update.message().chat().id(), update);
                                    try {
                                        Method method = listener.getMethod("onCommand", String.class, int.class, long.class, Update.class);
                                        viewMessage = (ViewMessage) method.invoke(listener.getDeclaredConstructor().newInstance(), update.message().text(), update.message().messageId(), update.message().chat().id(), update);
                                        if (viewMessage != null) {
                                            break;
                                        }
                                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                             NoSuchMethodException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            if (listener != null) {
                                viewMessage = listener.onCommand(update.message().text(), update.message().messageId(), update.message().chat().id(), update);
                            }
                        } else {
                            if (annotated != null) {
                                for (Class<?> listener : annotated) {
                                    //viewMessage = listener.onMessage(update.message().text(), update.message().messageId(), update.message().chat().id(), update);
                                    try {
                                        Method method = listener.getMethod("onMessage", String.class, int.class, long.class, Update.class);
                                        viewMessage = (ViewMessage) method.invoke(listener.getDeclaredConstructor().newInstance(), update.message().text(), update.message().messageId(), update.message().chat().id(), update);
                                        if (viewMessage != null) {
                                            break;
                                        }
                                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                             NoSuchMethodException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                            if (listener != null) {
                                viewMessage = listener.onMessage(update.message().text(), update.message().messageId(), update.message().chat().id(), update);
                            }
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
                        if (annotated != null) {
                            for (Class<?> listener : annotated) {
                                //viewMessage = listener.onCallbackQuery(update.callbackQuery().data(), update.callbackQuery().message().messageId(), update.callbackQuery().message().chat().id(), update);
                                try {
                                    Method method = listener.getMethod("onCallbackQuery", String.class, int.class, long.class, Update.class);
                                    viewMessage = (ViewMessage) method.invoke(listener.getDeclaredConstructor().newInstance(), update.callbackQuery().data(), update.callbackQuery().message().messageId(), update.callbackQuery().message().chat().id(), update);
                                    if (viewMessage != null) {
                                        break;
                                    }
                                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                         NoSuchMethodException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        if (listener != null) {
                            viewMessage = listener.onCallbackQuery(update.callbackQuery().data(), update.callbackQuery().message().messageId(), update.callbackQuery().message().chat().id(), update);
                        }
                    }
                }
                if (update.inlineQuery() != null) {
                    if (annotated != null) {
                        for (Class<?> listener : annotated) {
                            //viewMessage = listener.onInlineQuery(update.inlineQuery().query(), update.inlineQuery().id(), update.inlineQuery().from().id(), update);
                            try {
                                Method method = listener.getMethod("onInlineQuery", String.class, String.class, long.class, Update.class);
                                viewMessage = (ViewMessage) method.invoke(listener.getDeclaredConstructor().newInstance(), update.inlineQuery().query(), update.inlineQuery().id(), update.inlineQuery().from().id(), update);
                                if (viewMessage != null) {
                                    break;
                                }
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (listener != null) {
                        viewMessage = listener.onInlineQuery(update.inlineQuery().query(), update.inlineQuery().id(), update.inlineQuery().from().id(), update);
                    }
                }
                if (update.chosenInlineResult() != null) {
                    if (annotated != null) {
                        for (Class<?> listener : annotated) {
                            //viewMessage = listener.onChosenInlineResult(update.chosenInlineResult().resultId(), update.chosenInlineResult().from().id(), update.chosenInlineResult().query(), update);
                            try {
                                Method method = listener.getMethod("onChosenInlineResult", String.class, long.class, String.class, Update.class);
                                viewMessage = (ViewMessage) method.invoke(listener.getDeclaredConstructor().newInstance(), update.chosenInlineResult().resultId(), update.chosenInlineResult().from().id(), update.chosenInlineResult().query(), update);
                                if (viewMessage != null) {
                                    break;
                                }
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (listener != null) {
                        viewMessage = listener.onChosenInlineResult(update.chosenInlineResult().resultId(), update.chosenInlineResult().from().id(), update.chosenInlineResult().query(), update);
                    }
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
