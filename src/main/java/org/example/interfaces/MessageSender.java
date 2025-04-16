package org.example.interfaces;

import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageSender {
    Message sendText(long chatId, String text);
    void editMsg(Long chatId, Integer messageId, String text);
    void deleteMessages(Long chatId);
    String formatWithSpacesAndDecimals(String input);
}