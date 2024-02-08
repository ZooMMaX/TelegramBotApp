package ru.zoommax.botapp.view;

import com.pengrad.telegrambot.model.request.*;
import lombok.Builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Builder
public class KeyboardMarkup {
    String buttonsNames;
    String buttonsCallbackData;

    public InlineKeyboardMarkup getInlineKeyboard() {
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


        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        for (int i = 0; i < names.size(); i++) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (int j = 0; j < names.get(i).size(); j++) {
                buttons.add(new InlineKeyboardButton(names.get(i).get(j)).callbackData(callbackData.get(i).get(j)));
            }
            markup.addRow(Arrays.copyOf(buttons.toArray(), buttons.size(), InlineKeyboardButton[].class));
        }
        return markup;
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
