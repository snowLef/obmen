package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CurrencyType {
    MULTIPLICATION ("умножение"),
    DIVISION ("деление");

    private final String text;
}
