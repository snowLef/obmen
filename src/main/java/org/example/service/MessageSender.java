package org.example.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@RequiredArgsConstructor
public class MessageSender {
    private final TelegramService telegramService;

    public Message sendText(long chatId, String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        return telegramService.send(message);
    }

    public void editMsg(long chatId, Integer messageId, String newText) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setMessageId(messageId);
        message.setText(newText);
        telegramService.edit(message);
    }

    public Message sendMsg(SendMessage message) {
        return telegramService.send(message);
    }


    public void delete(Integer id) {
        DeleteMessage message = new DeleteMessage();
        message.setMessageId(id);
        telegramService.delete(message);
    }
}