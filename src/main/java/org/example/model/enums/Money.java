package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Money {
    USDT ("USDT", "\nUSDT"),
    USD ("USD", "USD"),
    EUR ("EUR", "EUR"),
    USDW ("USD (Б)", "USD (Б)"),
    YE ("Y.E.", "Y.E."),
    RUB ("RUB", "RUB");

    private final String name;
    private final String nameForBalance;

    public static Money valueOfName(String name) {
        for (Money m : values()) {
            if (m.name.equalsIgnoreCase(name.trim())) {
                return m;
            }
        }
        throw new IllegalArgumentException("Валюта не найдена: " + name);
    }
}
