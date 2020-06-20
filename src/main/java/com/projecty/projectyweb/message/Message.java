package com.projecty.projectyweb.message;

import com.projecty.projectyweb.message.attachment.Attachment;
import com.projecty.projectyweb.user.User;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_recipient_id")
    private User recipient;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date sendDate;
    private Date seenDate;

    @NotBlank
    private String title;

    @NotBlank
    private String text;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Attachment> attachments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private Message replyToMessage;

    @Transient
    private String recipientUsername;
}
