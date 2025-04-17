package org.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

@Entity
@Table(name = "currency")
@Getter
@Setter
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "currency_balances", joinColumns = @JoinColumn(name = "currency_id"))
    @MapKeyColumn(name = "balance_type")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "amount")
    private Map<BalanceType, Double> balances = new EnumMap<>(BalanceType.class);

    public Double getBalance(BalanceType type) {
        return balances.getOrDefault(type, 0.0);
    }

    public void updateBalance(BalanceType type, double newAmount) {
        balances.put(type, newAmount);
    }

    public void move(BalanceType from, BalanceType to, double amount) {
        double fromBalance = getBalance(from);
        double toBalance = getBalance(to);
        if (fromBalance >= amount) {
            balances.put(from, fromBalance - amount);
            balances.put(to, toBalance + amount);
        } else {
            throw new IllegalArgumentException("Not enough balance in " + from);
        }
    }
}


