package org.example.model;

public enum DealType {
    BUY,
    SELL;

    // Метод для проверки, является ли сделка покупкой
    public boolean isBuy() {
        return this == BUY;
    }

    // Метод для проверки, является ли сделка продажей
    public boolean isSell() {
        return this == SELL;
    }
}