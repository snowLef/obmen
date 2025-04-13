package org.example.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deals")
@Getter
@Setter
public class Deal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // или GenerationType.AUTO
    private Long id;

    private Money moneyFrom;
    private Money moneyTo;
    private String buyerName;
    private Double amount;
    private Double amountFrom;
    private Double exchangeRate;
    private DealType dealType;
    private Boolean isCustom = false;

}
