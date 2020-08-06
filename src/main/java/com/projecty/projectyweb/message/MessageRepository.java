package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByRecipientAndHasReplyIsFalseOrderBySendDateDesc(User recipient, Pageable pageable);

    Page<Message> findBySenderAndHasReplyIsFalseOrderBySendDateDesc(User sender, Pageable pageable);

    @Query("select m from Message m where (m.sender=?1 or m.recipient=?1) and m.hasReply=false order by m.sendDate desc")
    Page<Message> findBySenderOrRecipientAndHasReplyIsFalseOrderBySendDateDesc(User user, Pageable pageable);

    List<Message> findByRecipientAndSeenDateIsNull(User recipient);
}
