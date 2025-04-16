package org.example.util;

import lombok.RequiredArgsConstructor;
import org.example.handler.CallbackHandler;
import org.example.handler.MessageHandler;
import org.example.interfaces.CallbackProcessor;
import org.example.interfaces.MessageProcessor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class UpdateFacade {
    private final MessageProcessor messageProcessor;
    private final CallbackProcessor callbackProcessor;

    public void process(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            messageProcessor.process(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackProcessor.process(update.getCallbackQuery());
        }
    }
}
