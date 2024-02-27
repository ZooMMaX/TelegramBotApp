package ru.zoommax.botapp.view;

import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;

import java.util.HashMap;
import java.util.List;

public class KeyboardMarkupsArrays {
    public HashMap<String, List<InlineKeyboardMarkup>> userKeyboardMarkups;
    private static KeyboardMarkupsArrays instance;

    private KeyboardMarkupsArrays() {
        userKeyboardMarkups = new HashMap<>();
    }

    public static KeyboardMarkupsArrays getInstance() {
        if (instance == null) {
            instance = new KeyboardMarkupsArrays();
        }
        return instance;
    }
}
