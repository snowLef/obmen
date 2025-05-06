package org.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.model.enums.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "deals")
@Getter
@Setter
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "deal_money_from", joinColumns = @JoinColumn(name = "deal_id"))
    private List<CurrencyAmount> moneyFrom = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "deal_money_to", joinColumns = @JoinColumn(name = "deal_id"))
    private List<CurrencyAmount> moneyTo = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private DealStatus status = DealStatus.NEW;

    private int MessageId;

    private String buyerName;

    private String cityFromTo;

    private Double exchangeRate;

    private long currentAmount;

    private String comment;

    private boolean approved = false;

    @Enumerated(EnumType.STRING)
    private DealType dealType;

    @Enumerated(EnumType.STRING)
    @Setter
    private AmountType amountType;

    @Setter
    private int currentCurrencyIndex;

    @Enumerated(EnumType.STRING)
    @Setter
    private CurrencyType currencyType;

    @Enumerated(EnumType.STRING)
    @Setter
    private PlusMinusType plusMinusType;

    @Enumerated(EnumType.STRING)
    @Setter
    private ChangeBalanceType changeBalanceType;

    @Enumerated(EnumType.STRING)
    @Setter
    private BalanceType balanceFrom;

    @Enumerated(EnumType.STRING)
    @Setter
    private BalanceType balanceTo;


    public List<Money> getMoneyFromList() {
        return moneyFrom.stream()
                .map(CurrencyAmount::getCurrency)
                .toList();
    }

    public List<Money> getMoneyToList() {
        return moneyTo.stream()
                .map(CurrencyAmount::getCurrency)
                .toList();
    }

    public void addMoneyFrom(Money money) {
        if (moneyFrom.stream().noneMatch(ca -> ca.getCurrency() == money)) {
            moneyFrom.add(new CurrencyAmount(money, 0L));
        }
    }

    public void removeMoneyFrom(Money money) {
        moneyFrom.removeIf(ca -> ca.getCurrency() == money);
    }

    public void addMoneyTo(Money money) {
        if (moneyTo.stream().noneMatch(ca -> ca.getCurrency() == money)) {
            moneyTo.add(new CurrencyAmount(money, 0L));
        }
    }

    public void removeMoneyTo(Money money) {
        moneyTo.removeIf(ca -> ca.getCurrency() == money);
    }

    public long getAmountFrom() {
        return moneyFrom.get(0).getAmount();
    }

    public long getAmountTo() {
        return moneyTo.get(0).getAmount();
    }

    public void setAmountFrom(long amount) {
        moneyFrom.get(0).setAmount(amount);
    }

    public void setAmountTo(long amount) {
        moneyTo.get(0).setAmount(amount);
    }

    /**
     * Откуда снимаем (OWN, FOREIGN, DEBT и т.п.).
     * Не забывайте устанавливать его при создании/настройке сделки.
     */
    private BalanceType balanceTypeFrom;

    /**
     * Куда кладём (OWN, FOREIGN, DEBT и т.п.).
     */
    private BalanceType balanceTypeTo;

    /** Время создания сделки в БД */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** Время отмены сделки (устанавливается вручную при отмене) */
    private LocalDateTime cancelledAt;

    /** Ник того, кто отменил (либо просто строка) */
    private String cancelledBy;

    private String createdBy;

    private String approvedBy;
}
