package ru.zoommax.botapp.utils;

import ru.zoommax.botapp.db.pojo.MessageRemoverPojo;
import ru.zoommax.botapp.db.pojo.UserPojo;
import ru.zoommax.botapp.view.KeyboardMarkup;
import ru.zoommax.botapp.view.ViewMessage;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class MessageRemover extends TimerTask {
    private final ExecutorService executor;
    public MessageRemover(ExecutorService executor) {
        this.executor = executor;
    }
    @Override
    public void run() {
        List<MessageRemoverPojo> messageRemoverPojo = new MessageRemoverPojo().findAll();
        for (MessageRemoverPojo mrp : messageRemoverPojo) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (mrp.getDate() <= System.currentTimeMillis()) {
                        UserPojo userPojo = new UserPojo();
                        userPojo.setChatId(Long.parseLong(mrp.getTg_id()));
                        userPojo = userPojo.find();
                        if (userPojo.getLastModifyViewMessage()/1000 == mrp.getLastModifyViewMessage()/1000) {
                            ViewMessage viewMessage = ViewMessage.builder()
                                    .chatId(Long.parseLong(mrp.getTg_id()))
                                    .message(mrp.getMessage())
                                    .callbackKeyboard(KeyboardMarkup.builder()
                                            .chatId(Long.parseLong(mrp.getTg_id()))
                                            .buttonsNames(mrp.getButtonNames())
                                            .buttonsCallbackData(mrp.getButtonsCallbackData())
                                            .build())
                                    .build();
                            viewMessage.run();
                            mrp.delete();
                        } else {
                            mrp.delete();
                        }
                    }
                }
            });
        }

    }
}
