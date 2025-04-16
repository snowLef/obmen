package org.example;

import org.example.interfaces.TelegramClient;
import org.example.interfaces.UpdateProcessor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class ObmenBot extends TelegramLongPollingBot implements TelegramClient {
    private final UpdateProcessor updateProcessor;

    public ObmenBot(UpdateProcessor updateProcessor) {
        this.updateProcessor = updateProcessor;
    }

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
        updateProcessor.process(update);
    }

    @Override
    public Message execute(SendMessage message) throws TelegramApiException {
        return super.execute(message);
    }

    @Override
    public void execute(EditMessageText message) throws TelegramApiException {
        super.execute(message);
    }

    @Override
    public void execute(DeleteMessage message) throws TelegramApiException {
        super.execute(message);
    }
}
