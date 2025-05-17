package org.example.ui;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyboardBuilder {

    private final List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    private List<InlineKeyboardButton> currentRow = new ArrayList<>();

    public static InlineKeyboardBuilder builder() {
        return new InlineKeyboardBuilder();
    }

    public InlineKeyboardBuilder button(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        currentRow.add(button);
        return this;
    }

    public InlineKeyboardBuilder newRow() {
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
            currentRow = new ArrayList<>();
        }
        return this;
    }

    public InlineKeyboardMarkup build() {
        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }
        return KeyboardUtils.buildInlineKeyboard(rows);
    }
}
