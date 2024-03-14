package ru.zoommax.botapp.view;

import com.pengrad.telegrambot.model.request.*;
import lombok.Builder;
import ru.zoommax.botapp.db.pojo.UserMarkupsPojo;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Builder
public class KeyboardMarkup {
    String buttonsNames;
    String buttonsCallbackData;
    InlineKeyboardMarkup inlineKeyboard;
    long chatId;

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

        for (int i = 0; i < names.size(); i += 4) {
            namesRow4.add(names.subList(i, Math.min(i + 4, names.size())));
        }

        for (int i = 0; i < callbackData.size(); i += 4) {
            callbackDataRow4.add(callbackData.subList(i, Math.min(i + 4, callbackData.size())));
        }

        List<InlineKeyboardMarkup> markups = new ArrayList<>();
        for (int x = 0; x < namesRow4.size(); x++) {
            List<List<String>> namesRow = namesRow4.get(x);
            List<List<String>> callbackDataRow = callbackDataRow4.get(x);
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            for (int i = 0; i < namesRow.size(); i++) {
                List<InlineKeyboardButton> buttons = new ArrayList<>();
                for (int j = 0; j < names.get(i).size(); j++) {
                    buttons.add(new InlineKeyboardButton(namesRow.get(i).get(j)).callbackData(callbackDataRow.get(i).get(j)));
                }
                markup.addRow(Arrays.copyOf(buttons.toArray(), buttons.size(), InlineKeyboardButton[].class));
            }
            InlineKeyboardButton buttonNext;
            if (x+1 < namesRow4.size()) {
                buttonNext = new InlineKeyboardButton("→").callbackData("nextButton:" + x);
            }else {
                buttonNext = new InlineKeyboardButton("→✖").callbackData("zero");
            }
            InlineKeyboardButton buttonPrev;
            if (x > 0) {
                buttonPrev = new InlineKeyboardButton("←").callbackData("prevButton:" + x);
            }else {
                buttonPrev = new InlineKeyboardButton("✖←").callbackData("zero");
            }
            markup.addRow(buttonPrev, buttonNext);
            markups.add(markup);
        }
        KeyboardMarkupsArrays.getInstance().userKeyboardMarkups.put(String.valueOf(chatId), markups);
        UserMarkupsPojo userMarkupsPojo = new UserMarkupsPojo();
        HashMap<String, List<byte[]>> markupsSerialized = new HashMap<>();
        for (String keySet : KeyboardMarkupsArrays.getInstance().userKeyboardMarkups.keySet()) {
            List<byte[]> markupSerialized = new ArrayList<>();
            for (InlineKeyboardMarkup markup : KeyboardMarkupsArrays.getInstance().userKeyboardMarkups.get(String.valueOf(keySet))) {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(markup);
                    out.flush();
                    markupSerialized.add(bos.toByteArray());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            markupsSerialized.put(keySet, markupSerialized);
        }
        userMarkupsPojo.setMarkups(markupsSerialized);
        userMarkupsPojo.insert();
        return markups.get(0);
    }

    public ReplyKeyboardMarkup getReplyKeyboard() {
        List<List<String>> names = new ArrayList<>();

        for (String name : buttonsNames.split("\n")) {
            List<String> row = new ArrayList<>(Arrays.asList(name.split(";")));
            names.add(row);
        }


        List<List<KeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            List<KeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < names.get(i).size(); j++) {
                buttons.add(new KeyboardButton(names.get(i).get(j)));
            }
            rows.add(buttons);
        }
        KeyboardButton[][] buttons = new KeyboardButton[rows.size()][];
        for (int i = 0; i < rows.size(); i++) {
            buttons[i] = Arrays.copyOf(rows.get(i).toArray(), rows.get(i).size(), KeyboardButton[].class);
        }
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(buttons);
        markup.oneTimeKeyboard(true);
        markup.resizeKeyboard(true);
        return markup;
    }
}
