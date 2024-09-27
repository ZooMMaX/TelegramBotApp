package ru.zoommax.botapp.view;

import com.pengrad.telegrambot.model.WebAppInfo;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import lombok.Builder;
import org.slf4j.Logger;
import ru.zoommax.botapp.BotApp;
import ru.zoommax.botapp.db.pojo.UserMarkupsPojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Builder
public class KBUnsafe {
    String buttonsNames;
    String buttonsCallbackData;
    InlineKeyboardMarkup inlineKeyboard;
    long chatId;
    Logger logger = org.slf4j.LoggerFactory.getLogger(KeyboardMarkup.class);

    public InlineKeyboardMarkup getInlineKeyboard() {

        if (inlineKeyboard != null) {
            return inlineKeyboard;
        }

        List<List<String>> names = new ArrayList<>();
        List<List<String>> callbackData = new ArrayList<>();

        for (String name : buttonsNames.split("\n")) {
            List<String> row = new ArrayList<>(Arrays.asList(name.split(";")));
            names.add(row);
        }


        for (String data : buttonsCallbackData.split("\n")) {
            List<String> row = new ArrayList<>(Arrays.asList(data.split(";")));
            callbackData.add(row);
        }

        List<List<List<String>>> namesRow4 = new ArrayList<>();
        List<List<List<String>>> callbackDataRow4 = new ArrayList<>();

        for (int i = 0; i < names.size(); i += BotApp.ButtonsRows) {
            namesRow4.add(names.subList(i, Math.min(i + BotApp.ButtonsRows, names.size())));
        }


        for (int i = 0; i < callbackData.size(); i += BotApp.ButtonsRows) {
            callbackDataRow4.add(callbackData.subList(i, Math.min(i + BotApp.ButtonsRows, callbackData.size())));
        }

        List<InlineKeyboardMarkup> markups = new ArrayList<>();
        for (int x = 0; x < namesRow4.size(); x++) {
            List<List<String>> namesRow = namesRow4.get(x);
            List<List<String>> callbackDataRow = callbackDataRow4.get(x);
            if (namesRow.size() != callbackDataRow.size()) {
                logger.error("Names and callback data size mismatch");
                return null;
            }
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            for (int i = 0; i < namesRow.size(); i++) {
                List<InlineKeyboardButton> buttons = new ArrayList<>();
                for (int j = 0; j < namesRow.get(i).size(); j++) {
                    if (callbackDataRow.get(i).get(j).startsWith("mapp")) {
                        buttons.add(new InlineKeyboardButton(namesRow.get(i).get(j)).webApp(new WebAppInfo(callbackDataRow.get(i).get(j).substring(4))));
                    } else if (callbackDataRow.get(i).get(j).startsWith("http") || callbackDataRow.get(i).get(j).startsWith("tg")) {
                        buttons.add(new InlineKeyboardButton(namesRow.get(i).get(j)).url(callbackDataRow.get(i).get(j)));
                    } else {
                        buttons.add(new InlineKeyboardButton(namesRow.get(i).get(j)).callbackData(callbackDataRow.get(i).get(j)));
                    }
                }
                markup.addRow(Arrays.copyOf(buttons.toArray(), buttons.size(), InlineKeyboardButton[].class));
            }
            InlineKeyboardButton buttonNext = null;
            if (x + 1 < namesRow4.size()) {
                buttonNext = new InlineKeyboardButton("→").callbackData("nextButton:" + x);
            }/*else {
                buttonNext = new InlineKeyboardButton("→✖").callbackData("zero");
            }*/
            InlineKeyboardButton buttonPrev = null;
            if (x > 0) {
                buttonPrev = new InlineKeyboardButton("←").callbackData("prevButton:" + x);
            }/*else {
                buttonPrev = new InlineKeyboardButton("✖←").callbackData("zero");
            }*/

            if (buttonNext != null && buttonPrev != null) {
                markup.addRow(buttonPrev, buttonNext);
            } else if (buttonNext != null) {
                markup.addRow(buttonNext);
            } else if (buttonPrev != null) {
                markup.addRow(buttonPrev);
            }
            markups.add(markup);
        }
        return markups.get(0);
    }
}
