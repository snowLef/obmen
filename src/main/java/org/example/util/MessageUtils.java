package org.example.util;

import org.example.model.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.example.ObmenBot;

import java.util.List;

@Component
public class MessageUtils {

    public static ObmenBot botInstance;
    private final UserService userService;

    @Autowired
    public MessageUtils(ObmenBot obmenBot, UserService userService) {
        this.botInstance = obmenBot;
        this.userService = userService;
    }

    public static Message sendText(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(String.valueOf(chatId))
                .text(text)
                .build();
        return sendMsg(message);
    }

    public static Message sendMsg(SendMessage message) {
        try {
            return botInstance.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace(); // лучше логировать
        }
        return null;
    }

    private static void del(DeleteMessage msg) {
        try {
            botInstance.execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteMessages(Long chatId) {
        User user = userService.getUser(chatId);
        List<Integer> messages = user.getMessages();

        messages.forEach(x -> {
            DeleteMessage deleteBotMessage = new DeleteMessage();
            deleteBotMessage.setChatId(chatId.toString());
            deleteBotMessage.setMessageId(x);
            del(deleteBotMessage);
        });
    }

    public static String formatWithSpacesAndDecimals(String input) {
        // 1. Заменим запятую на точку для парсинга
        String normalized = input.replace(",", ".");

        // 2. Преобразуем в число и округлим до 2 знаков
        double number = Double.parseDouble(normalized);
        String formatted = String.format("%.2f", number); // например: "1234567.89"

        // 3. Разделим обратно на целую часть и копейки
        String[] parts = formatted.split(",");
        String wholePart = parts[0];
        String fractionalPart = "," + parts[1]; // снова возвращаем запятую

        // 4. Разделим целую часть пробелами с конца
        StringBuilder reversed = new StringBuilder(wholePart).reverse();
        StringBuilder spaced = new StringBuilder();

        for (int i = 0; i < reversed.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                spaced.append(" ");
            }
            spaced.append(reversed.charAt(i));
        }

        // 5. Собираем всё обратно
        return spaced.reverse() + fractionalPart;
    }

    public static void editMsg(Long chatId, Integer messageId, String text) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId.toString());
        editMessage.setText(text);

        editMessage.setMessageId(messageId); // ID сообщения, которое бот хочет изменить

        editMessage.setReplyMarkup(null);

        try {
            botInstance.execute(editMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
