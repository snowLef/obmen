package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Money {
    USD ("Usd", "Usd"),
    EUR ("Eur", "Eur"),
    USDW ("UsdW", "UsdW"),
    RUB ("Rub", "Rub"),
    YE ("Y.e.", "Y.e."),
    USDT ("UsdT", "\nUsdT");

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
