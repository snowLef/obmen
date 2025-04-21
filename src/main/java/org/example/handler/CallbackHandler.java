package org.example.handler;

import org.example.handler.callback.CallbackCommandHandler;
import org.example.model.*;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.List;

@Component
public class CallbackHandler {

    private final List<CallbackCommandHandler> handlers;
    private final UserService userService;

    @Autowired
    public CallbackHandler(List<CallbackCommandHandler> handlers, UserService userService) {
        this.handlers = handlers;
        this.userService = userService;
    }

    public void process(CallbackQuery query) {
        long chatId = query.getMessage().getChatId();
        String data = query.getData();
        User user = userService.getOrCreate(chatId);

        System.out.printf("[%d] Статус: %s, CallBackData: %s%n", chatId, user.getStatus(), data);

        for (CallbackCommandHandler handler : handlers) {
            if (handler.supports(data)) {
                handler.handle(query, user);
                return;
            }
        }

        // fallback
        throw new UnsupportedOperationException("Неизвестный callback: " + data);
    }
}
