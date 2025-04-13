package org.example.model;

import lombok.Getter;
import lombok.Setter;
import org.example.state.Status;

import javax.persistence.*;
import java.util.ArrayList;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // или GenerationType.AUTO
    private Long id;
    private String name;
    private Long chatId;
    private Status status = Status.IDLE;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "currentDeal", foreignKey = @ForeignKey(name = "fk_current_deal"))
    private Deal currentDeal;
    private ArrayList<Integer> messages;
    private Integer messageToEdit;

    public void addMessage(Integer id) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
            messages.add(id);
        } else {
            messages.add(id);
        }
    }
}


