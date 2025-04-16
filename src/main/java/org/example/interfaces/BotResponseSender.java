package org.example.interfaces;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface BotResponseSender {
    Message sendText(Long chatId, String text);
    void editMessage(Long chatId, Integer messageId, String text);
    void deleteMessage(Integer messageId);
    void deleteMessages(Long chatId, List<Integer> messageIds);
    Message sendMsg(SendMessage message);
    void editMsg(long chatId, Integer messageId, String text);
}
