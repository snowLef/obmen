package org.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.state.Status;

import java.util.ArrayList;

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
    private Status status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "currentDeal", foreignKey = @ForeignKey(name = "fk_current_deal"))
    private Deal currentDeal;

    private ArrayList<Integer> messages;
    private Integer messageToEdit;
    private AmountType amountType;
    private CurrencyType currencyType;

    public User(Long chatId, Status status) {
        this.chatId = chatId;
        this.status = status;
    }

    public void addMessage(Integer id) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
            messages.add(id);
        } else {
            messages.add(id);
        }
    }
}


