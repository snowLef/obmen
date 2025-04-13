package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Money {
    USD ("Usd"),
    EUR ("Eur"),
    USDW ("UsdW"),
    RUB ("Rub"),
    USDT ("\nUsdT");

    private final String name;
}
