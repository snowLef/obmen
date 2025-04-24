package org.example.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.enums.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {

    public User(Long chatId, Status status) {
        this.chatId = chatId;
        this.status = status;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // или GenerationType.AUTO
    private Long id;

    private String name;

    private Long chatId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Setter
    private Status previousStatus;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "currentDeal", foreignKey = @ForeignKey(name = "fk_current_deal"))
    @Setter
    private Deal currentDeal;

    @ElementCollection
    @Setter
    private List<Integer> messages;

    @Setter
    private Integer messageToEdit;

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

    @Transient // Не сохраняем в БД, если используем JPA
    private Deque<Status> statusHistory = new ArrayDeque<>();

    public void pushStatus(Status newStatus) {
        if (this.status != null) {
            previousStatus = this.status;
        }
        this.status = newStatus;
    }

    public Status popStatus() {
        return previousStatus;
    }

    @Transactional
    public void addMessage(Integer messageId) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(messageId);
    }

}


