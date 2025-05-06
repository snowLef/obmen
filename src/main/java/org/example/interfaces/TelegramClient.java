package org.example.interfaces;

import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramClient {
    void deleteMessage(DeleteMessage message) throws TelegramApiException;
}
