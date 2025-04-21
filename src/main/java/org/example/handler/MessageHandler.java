package org.example.handler;

import lombok.RequiredArgsConstructor;
import org.example.handler.state.UserStateHandler;
import org.example.model.*;
import org.example.service.UserService;
import org.example.model.enums.Status;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final UserService userService;
    private final Map<Status, UserStateHandler> stateHandlers;

    public void process(Message message) {
        if (message == null || message.getText() == null) return;

        long chatId = message.getChatId();
        User user = userService.getOrCreate(chatId);
        Status status = user.getStatus();

        System.out.println("[" + chatId + "] Статус: " + status + ", Текст: " + message.getText());

        UserStateHandler handler = stateHandlers.get(status);
        if (handler != null) {
            handler.handle(message, user);
        } else {
            System.err.println("Нет обработчика для статуса: " + status);
        }
    }
}
