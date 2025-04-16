package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.ObmenBot;
import org.example.interfaces.TelegramOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramService implements TelegramOperations {

    private final ObmenBot bot;

    @Autowired
    public TelegramService(ObmenBot bot) {
        this.bot = bot;
    }

    public Message send(SendMessage message) {
        try {
            return bot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void edit(EditMessageText message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(DeleteMessage message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
