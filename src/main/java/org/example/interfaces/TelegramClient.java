package org.example.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramClient {
    Message execute(SendMessage message) throws TelegramApiException;
    void execute(EditMessageText message) throws TelegramApiException;
    void execute(DeleteMessage message) throws TelegramApiException;
}
