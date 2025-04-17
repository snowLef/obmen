package org.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "deals")
@Getter
@Setter
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // или GenerationType.AUTO
    private Long id;

    @Enumerated(EnumType.STRING)
    private Money moneyFrom;

    @Enumerated(EnumType.STRING)
    private Money moneyTo;

    private String buyerName;

    private Double currentAmount;

    private Double amountTo;

    private Double amountFrom;

    private Double exchangeRate;

    @Enumerated(EnumType.STRING)
    private DealType dealType;

}
