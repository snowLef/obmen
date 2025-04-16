package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ObmenBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramSenderImpl implements TelegramSender {

    private final ObmenBot obmenBot;

    @Override
    public void send(SendMessage message) {
        try {
            obmenBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void send(EditMessageText message) {
        try {
            obmenBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to edit message", e);
        }
    }

    @Override
    public Message sendText(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            return obmenBot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void editMsg(Long chatId, Integer messageToEdit, String s) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(messageToEdit);
        message.setText(s);
        try {
            obmenBot.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
