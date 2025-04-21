package org.example.handler.callback;

import org.example.model.User;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

public interface CallbackCommandHandler {
    boolean supports(String data);
    void handle(CallbackQuery query, User user);
}
