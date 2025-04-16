package org.example.service;

import org.example.handler.CallbackHandler;
import org.example.handler.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class UpdateRouter {
    private MessageHandler messageHandler;
    private CallbackHandler callbackHandler;

    @Autowired
    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Autowired
    public void setCallbackHandler(CallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    public void route(Update update) {
        if (update.hasMessage()) {
            messageHandler.process(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.process(update.getCallbackQuery());
        }
    }
}