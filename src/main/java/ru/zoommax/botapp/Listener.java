package ru.zoommax.botapp;

import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import ru.zoommax.botapp.view.ViewMessage;

public interface Listener {
    ViewMessage onMessage(String message, int messageId, long chatId, Update update);
    ViewMessage onCommand(String command, int messageId, long chatId, Update update);
    ViewMessage onPicture(PhotoSize[] photoSize, String caption, int messageId, long chatId, Update update);
    ViewMessage onCallbackQuery(String data, int messageId, long chatId, Update update);
    ViewMessage onInlineQuery(String query, String queryId, long chatId, Update update);
    ViewMessage onChosenInlineResult(String resultId, long queryId, String chatId, Update update);
}
