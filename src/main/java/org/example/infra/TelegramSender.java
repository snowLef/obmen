package org.example.infra;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface TelegramSender {
    Message send(SendMessage message);
    void send(EditMessageText message);
    Message sendText(Long chatId, String text);
    void editMsg(Long chatId, Integer messageToEdit, String s);
    void deleteMessage(Long chatId, Integer messageToDelete);
    public Message sendInlineKeyboard(Long chatId, String text, InlineKeyboardMarkup markup);
    public void editMsgWithKeyboard(Long chatId, Integer messageToEdit, String s);
}
