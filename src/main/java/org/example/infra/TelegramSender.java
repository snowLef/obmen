package org.example.infra;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface TelegramSender {
    Message send(SendMessage message);

    Message sendText(Long chatId, String text);

    void editMsg(Long chatId, Integer messageToEdit, String s);

    void deleteMessage(Long chatId, Integer messageToDelete);

    Message sendInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup markup);

    void editMsgWithKeyboard(Long chatId, Integer messageToEdit, String s);

    void sendTextWithKeyboard(Long chatId, String text);

    Message sendWithMarkup(long chatId, String text, InlineKeyboardMarkup markup);

    void editMessageReplyMarkup(long chatId, int messageId, InlineKeyboardMarkup markup);

    void deleteMessages(long chatId, List<Integer> ids);
}
