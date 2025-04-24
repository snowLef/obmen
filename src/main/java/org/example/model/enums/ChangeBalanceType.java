package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChangeBalanceType {
    ADD ("Пополнение"),
    WITHDRAWAL("Вывод");

    private final String type;
}
