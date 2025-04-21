package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Money {
    USD ("Usd", "USD"),
    EUR ("Eur", "EUR"),
    USDW ("UsdW", "USD (Б)"),
    RUB ("Rub", "RUB"),
    YE ("Y.e.", "Y.E."),
    USDT ("UsdT", "\nUSDT");

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
