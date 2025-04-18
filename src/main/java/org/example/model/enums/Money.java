package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Money {
    USD ("Usd"),
    EUR ("Eur"),
    USDW ("UsdW"),
    RUB ("Rub"),
    YE ("Y.e."),
    USDT ("\nUsdT");

    private final String name;

    public static Money valueOfName(String name) {
        for (Money m : values()) {
            if (m.name.equalsIgnoreCase(name.trim())) {
                return m;
            }
        }
        throw new IllegalArgumentException("Валюта не найдена: " + name);
    }
}
