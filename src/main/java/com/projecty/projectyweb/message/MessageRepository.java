package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByRecipientOrderBySendDateDesc(User recipient, Pageable pageable);

    Page<Message> findBySenderOrderBySendDateDesc(User sender, Pageable pagable);

    List<Message> findByRecipientAndSeenDateIsNull(User recipient);
}
