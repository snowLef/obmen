package org.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.infra.TelegramSender;
import org.example.model.enums.BalanceType;
import org.springframework.beans.factory.annotation.Autowired;

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
    private Map<BalanceType, Long> balances = new EnumMap<>(BalanceType.class);

    public long getBalance(BalanceType type) {
        return balances.getOrDefault(type, 0L);
    }

    public void updateBalance(BalanceType type, long newAmount) {
        balances.put(type, newAmount);
    }

    public void move(BalanceType from, BalanceType to, long amount) {
        long fromBalance = getBalance(from);
        long toBalance = getBalance(to);
        if (fromBalance >= amount || from == BalanceType.OWN) {
            balances.put(from, fromBalance - amount);
            balances.put(to, toBalance + amount);
        } else {
            throw new IllegalArgumentException("Not enough balance in " + from);
        }
    }
}


