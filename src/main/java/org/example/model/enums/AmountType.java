package org.example.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AmountType {
    RECEIVE ("получения"),
    GIVE ("выдачи");

    private final String text;
}
