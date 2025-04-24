package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DealType {
    BUY ("Покупка"),
    SELL ("Продажа"),
    CUSTOM ("Валютный обмен"),
    PLUS_MINUS("+/-"),
    MOVING_BALANCE ("Перемещение"),
    CHANGE_BALANCE ("Изменение"),
    TRANSPOSITION ("Перестановка"),
    INVOICE ("Invoice");

    private final String type;
}