package org.example.handler.state;

import org.example.model.User;
import org.example.model.enums.Status;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface UserStateHandler {
    void handle(Message message, User user);
    Status getSupportedStatus();  // Метод для получения поддерживаемого статуса
}
