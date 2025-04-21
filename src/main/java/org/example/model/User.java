package org.example.model;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.model.enums.AmountType;
import org.example.model.enums.ChangeBalanceType;
import org.example.model.enums.CurrencyType;
import org.example.model.enums.Status;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // или GenerationType.AUTO
    private Long id;

    private String name;

    private Long chatId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "currentDeal", foreignKey = @ForeignKey(name = "fk_current_deal"))
    private Deal currentDeal;

    @ElementCollection
    private List<Integer> messages;

    private Integer messageToEdit;

    @Enumerated(EnumType.STRING)
    private AmountType amountType;

    private int currentCurrencyIndex;

    @Enumerated(EnumType.STRING)
    private CurrencyType currencyType;

    @Enumerated(EnumType.STRING)
    private ChangeBalanceType changeBalanceType;

    public User(Long chatId, Status status) {
        this.chatId = chatId;
        this.status = status;
    }

    @Transactional
    public void addMessage(Integer messageId) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(messageId);
    }
}


