package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.handler.CallbackHandler;
import org.example.handler.MessageHandler;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class UpdateRouter {
    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;

    public void route(Update update) {
        if (update.hasMessage()) {
            messageHandler.process(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.process(update.getCallbackQuery());
        }
    }
}