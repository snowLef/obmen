package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.handler.CallbackHandler;
import org.example.handler.MessageHandler;
import org.example.interfaces.UpdateProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@RequiredArgsConstructor
public class UpdateDispatcher implements UpdateProcessor {
    @Lazy
    private final MessageHandler messageHandler;
    @Lazy
    private final CallbackHandler callbackHandler;

    @Override
    public void process(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageHandler.process(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.process(update.getCallbackQuery());
        }
    }
}