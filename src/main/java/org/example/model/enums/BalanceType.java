package org.example.model.enums;

import lombok.Getter;

@Getter
public enum BalanceType {
    OWN("Наш"),
    FOREIGN("Чужой"),
    DEBT("Долг");

    private final String displayName;

    BalanceType(String displayName) {
        this.displayName = displayName;
    }

}
