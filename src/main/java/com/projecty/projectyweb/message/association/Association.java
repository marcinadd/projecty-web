package com.projecty.projectyweb.message.association;

import com.projecty.projectyweb.message.Message;
import com.projecty.projectyweb.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Association {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    @PreRemove
    public void preRemove() {
        if(message.getSender().equals(user)) {
            message.setSender(null);
        }else {
            message.setRecipient(null);
        }
    }
}
