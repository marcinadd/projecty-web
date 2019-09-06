package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRecipientOrderBySendDateDesc(User recipient);
    List<Message> findBySenderOrderBySendDateDesc(User sender);
    List<Message> findByRecipientAndSeenDateIsNull(User recipient);
}
