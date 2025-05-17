package org.example.ui;

import org.example.model.enums.Money;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class KeyboardUtils {

    private KeyboardUtils() {}

    /**
     * Строит одну строку InlineKeyboard с кнопками валют.
     *
     * @param currencies   набор валют
     * @param includeDone  добавить кнопку "✅ Готово" в конец
     */
    public static InlineKeyboardMarkup buildCurrencyKeyboard(Collection<Money> currencies,
                                                             boolean includeDone) {
        List<InlineKeyboardButton> row = new ArrayList<>(currencies.stream()
                .map(m -> InlineKeyboardButton.builder()
                        .text(m.getName())
                        .callbackData(m.getName())
                        .build())
                .toList());


        if (includeDone) {
            row.add(InlineKeyboardButton.builder()
                    .text("✅")
                    .callbackData("done")
                    .build());
        }

        // одноуровневая клавиатура
        return buildInlineKeyboard(List.of(row));
    }

    /**
     * Строит ReplyKeyboardMarkup с опциями.
     *
     * @param rows       список рядов кнопок
     */
    public static ReplyKeyboardMarkup buildReplyKeyboard(List<KeyboardRow> rows) {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        return markup;
    }

    /**
     * Строит InlineKeyboardMarkup из рядов Inline-кнопок.
     *
     * @param rows список рядов InlineKeyboardButton
     */
    public static InlineKeyboardMarkup buildInlineKeyboard(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardButton button(String text, String callbackData) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callbackData);
        return btn;
    }

}
