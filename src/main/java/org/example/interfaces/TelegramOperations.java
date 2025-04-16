package org.example.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface TelegramOperations {
    Message send(SendMessage message);
    void edit(EditMessageText message);
    void delete(DeleteMessage message);
}
