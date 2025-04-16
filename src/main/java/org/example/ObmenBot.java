package org.example;

import lombok.RequiredArgsConstructor;
import org.example.handler.CallbackHandler;
import org.example.handler.MessageHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class ObmenBot extends TelegramLongPollingBot {

    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;

    @Override
    public String getBotUsername() {
        return "obmen_bot";
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageHandler.process(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.process(update.getCallbackQuery());
        }
    }

}
