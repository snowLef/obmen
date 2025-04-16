package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.interfaces.BotResponseSender;
import org.example.interfaces.TelegramClient;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramResponseSender implements BotResponseSender {
    private final TelegramClient telegramClient;

    @Override
    public Message sendText(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        try {
            return telegramClient.sendMessage(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void editMessage(Long chatId, Integer messageId, String text) {

    }

    @Override
    public void deleteMessage(Integer messageId) {

    }

    @Override
    public void deleteMessages(Long chatId, List<Integer> messageIds) {

    }

    public Message sendMsg(SendMessage message) {
        return null;
    }

    @Override
    public void editMsg(long chatId, Integer messageId, String text) {

    }
}