package com.projecty.projectyweb.chat;

import com.projecty.projectyweb.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;

@Entity
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_sender_id")
    private User sender;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_recipient_id")
    private User recipient;
    private Date sendDate;
    private Date seenDate;
    @NotBlank
    private String text;

    public ChatMessage() {
    }

    public ChatMessage(User sender, User recipient, @NotBlank String text, Date sendDate) {
        this.sender = sender;
        this.recipient = recipient;
        this.sendDate = sendDate;
        this.text = text;
    }
}
