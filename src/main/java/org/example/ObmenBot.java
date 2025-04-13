package org.example;

import org.example.handler.CallbackHandler;
import org.example.handler.MessageHandler;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ObmenBot extends TelegramLongPollingBot {

    private final MessageHandler messageHandler = new MessageHandler();
    private final CallbackHandler callbackHandler = new CallbackHandler();

    @Override
    public String getBotUsername() {
        return "obmen_bot";
    }

    @Override
    public String getBotToken() {
        return "7553067691:AAEh_m5NKOksz_FaO-kKn8X9uyaYhSI5SMQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageHandler.handle(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handle(update.getCallbackQuery());
        }
    }

}
