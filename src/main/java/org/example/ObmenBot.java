package org.example;

import org.example.util.UpdateFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class ObmenBot extends TelegramLongPollingBot {

    private final UpdateFacade updateFacade;

    @Autowired
    public ObmenBot(UpdateFacade updateFacade) {
        this.updateFacade = updateFacade;
    }


    @Override
    public String getBotUsername() {
        return "obmen_bot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

//    @Override
//    public void onUpdateReceived(Update update) {
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            updateProcessor.processMessage(update.getMessage());
//        } else if (update.hasCallbackQuery()) {
//            updateProcessor.processCallbackQuery(update.getCallbackQuery());
//        }
//    }

    @Override
    public void onUpdateReceived(Update update) {
        updateFacade.process(update);
    }

}
