package org.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.model.enums.BalanceType;
import org.example.model.enums.Money;

import java.time.Instant;

@Entity
@Table(name = "balance_events")
@Getter
@Setter
public class BalanceEvent {

    protected BalanceEvent() {
    }

    public BalanceEvent(Long dealId, Money currency, BalanceType type, long delta) {
        this.dealId = dealId;
        this.currency = currency;
        this.type = type;
        this.delta = delta;
    }

    @Id
    @GeneratedValue
    Long id;

    private Long dealId;

    @Enumerated(EnumType.STRING)
    private Money currency;

    @Enumerated(EnumType.STRING)
    private BalanceType type;

    private long delta;

    private Instant created = Instant.now();

}
