package org.example.model;

import jakarta.persistence.*;
import lombok.*;

import org.example.model.enums.Money;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyAmount {

//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) // или GenerationType.AUTO
//    private Long id;

    @Enumerated(EnumType.STRING)
    private Money currency;

    private long amount;
}
