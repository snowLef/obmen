package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlusMinusType {
    GET("Принимаем +"),
    GIVE("Отдаем +"),
    LEND("Даем в долг"),
    DEBT_REPAYMENT("Возврат долга");

    private final String type;
}
