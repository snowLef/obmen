package org.example.util;

import org.springframework.stereotype.Component;

@Component
public class MessageUtils {
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
}
