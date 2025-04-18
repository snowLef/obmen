package org.example.handler.command;

import org.telegram.telegrambots.meta.api.objects.Message;

public record CommandContext(Message message) {
    public long chatId() {
        return message.getChatId();
    }
    public int msgId() {
        return message.getMessageId();
    }
}