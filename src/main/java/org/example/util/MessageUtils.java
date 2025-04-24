package org.example.util;

import org.example.infra.TelegramSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class MessageUtils {

    TelegramSender telegramSender;

    @Autowired
    public void setTelegramSender(TelegramSender telegramSender) {
        this.telegramSender = telegramSender;
    }

    public String formatWithSpacesAndDecimals(long input) {
        // 2. Преобразуем в число и округлим до 2 знаков
        //long number = Long.parseLong(input);

        // 4. Разделим целую часть пробелами с конца
        StringBuilder reversed = new StringBuilder(String.valueOf(input)).reverse();
        StringBuilder spaced = new StringBuilder();

        for (int i = 0; i < reversed.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                spaced.append("'");
            }
            spaced.append(reversed.charAt(i));
        }

        // 5. Собираем всё обратно
        return spaced.reverse().toString();
    }

    public void deleteMsgs(long chatId, List<Integer> ids) {
        ids.forEach(
                x -> telegramSender.deleteMessage(chatId, x)
        );
    }

    public void sendFormattedText(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("MarkdownV2"); // Включаем Markdown

        // Экранируем специальные символы (обязательно!)
        message.setText(escapeMarkdown(text));

        telegramSender.send(message);
    }

    // Экранирование спецсимволов для MarkdownV2
    public String escapeMarkdown(String text) {
        return text.replace("-", "\\-")
                .replace(".", "\\.")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("!", "\\!")
//                .replace(">", "\\>")
                .replace("=", "\\=")
                .replace("+", "\\+");
    }
}
