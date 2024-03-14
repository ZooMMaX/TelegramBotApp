import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import ru.zoommax.botapp.Listener;
import ru.zoommax.botapp.db.pojo.UserPojo;
import ru.zoommax.botapp.view.KeyboardMarkup;
import ru.zoommax.botapp.view.ViewMessage;

import java.io.File;

import static ru.zoommax.botapp.BotApp.bot;

public class Listeners implements Listener {
    @Override
    public ViewMessage onMessage(String message, int messageId, long chatId, Update update) {
        KeyboardMarkup keyboardMarkup = KeyboardMarkup.builder()
                .buttonsNames("Button 1;Button 2\n" +
                        "Button 3;Button 4")
                .buttonsCallbackData("Button 1;Button 2\n" +
                        "Button 3;Button 4")
                .build();
        return ViewMessage.builder()
                .message(message)
                .chatId(chatId)
                .callbackKeyboard(keyboardMarkup)
                .build();
    }

    @Override
    public ViewMessage onCommand(String command, int messageId, long chatId, Update update) {
        if (command.equals("/start")) {
            UserPojo userPojo = new UserPojo();
            userPojo.setChatId(chatId);
            userPojo = userPojo.find();
            userPojo.setViewMessageId(bot.execute(new SendMessage(update.message().chat().id(), "Bot starting...")).message().messageId());
            userPojo.insert();
            KeyboardMarkup keyboardMarkup = KeyboardMarkup.builder()
                    .buttonsNames("Button 1;Button 2\n" +
                            "Button 3;Button 4")
                    .buttonsCallbackData("Button 1;Button 2\n" +
                            "Button 3;Button 4")
                    .build();
            return ViewMessage.builder()
                    .message("Hello, " + update.message().from().firstName() + "!")
                    .chatId(chatId)
                    .callbackKeyboard(keyboardMarkup)
                    .build();
        }
        return null;
    }

    @Override
    public ViewMessage onPicture(PhotoSize[] photoSize, String caption, int messageId, long chatId, Update update) {
        return null;
    }

    @Override
    public ViewMessage onCallbackQuery(String data, int messageId, long chatId, Update update) {
        KeyboardMarkup keyboardMarkup = KeyboardMarkup.builder()
                .buttonsNames("Button 1;Button 2\n" +
                        "Button 3;Button 4")
                .buttonsCallbackData("Button 1;Button 2\n" +
                        "Button 3;Button 4")
                .build();
        return ViewMessage.builder()
                .message(messageId + " " + data)
                .image(new File("screen01.jpg"))
                .chatId(chatId)
                .build();
    }

    @Override
    public ViewMessage onInlineQuery(String query, String queryId, long chatId, Update update) {
        return null;
    }

    @Override
    public ViewMessage onChosenInlineResult(String resultId, long queryId, String chatId, Update update) {
        return null;
    }
}
