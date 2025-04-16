package org.example.util;

import lombok.RequiredArgsConstructor;
import org.example.interfaces.TelegramOperations;
import org.example.service.UserService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageUtils {
    private final UserService userService;

    public String formatWithSpacesAndDecimals(String input) {
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
//    public Message sendText(long chatId, String text) {
//        SendMessage message = SendMessage.builder()
//                .chatId(String.valueOf(chatId))
//                .text(text)
//                .build();
//        return sendMsg(message);
//    }
//
//    public Message sendMsg(SendMessage message) {
//        return telegramService.send(message);
//    }
//
//    private void del(DeleteMessage msg) {
//        telegramService.delete(msg);
//    }
//
//    public void deleteMessages(Long chatId) {
//        User user = userService.getUser(chatId);
//        List<Integer> messages = user.getMessages();
//
//        messages.forEach(x -> {
//            DeleteMessage deleteBotMessage = new DeleteMessage();
//            deleteBotMessage.setChatId(chatId.toString());
//            deleteBotMessage.setMessageId(x);
//            del(deleteBotMessage);
//        });

//    }
//    public void editMsg(Long chatId, Integer messageId, String text) {
//        EditMessageText editMessage = new EditMessageText();
//        editMessage.setChatId(chatId.toString());
//        editMessage.setText(text);
//        editMessage.setMessageId(messageId);
//        editMessage.setReplyMarkup(null);
//
//        telegramService.edit(editMessage);

//    }

}
