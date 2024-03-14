package ru.zoommax.botapp.utils;

import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.response.GetFileResponse;

import static ru.zoommax.botapp.BotApp.bot;

public class DownloadFile {
    public static String getUrl(GetFile getFile){
        GetFileResponse response = bot.execute(getFile);
        File file = response.file();
        return bot.getFullFilePath(file);
    }
}
