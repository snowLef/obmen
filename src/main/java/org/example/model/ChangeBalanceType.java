package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChangeBalanceType {
    GET("Принимаем +"),
    GIVE("Отдаем +"),
    LEND("Даем в долг"),
    DEBT_REPAYMENT("Возврат долга");

    private final String type;
}
