package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DealType {
    BUY ("Покупка"),
    SELL ("Продажа"),
    CUSTOM ("Сложный обмен"),
    CHANGE_BALANCE ("Изменение баланса"),
    TRANSPOSITION ("Перестановка"),
    INVOICE ("Invoice");

    private final String type;
}