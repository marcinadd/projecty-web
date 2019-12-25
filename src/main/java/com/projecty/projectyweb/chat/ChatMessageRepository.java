package com.projecty.projectyweb.chat;

import com.projecty.projectyweb.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("select m from ChatMessage m where m.sender=?1 or m.recipient=?1 order by m.id desc")
    Page<ChatMessage> findFirstByRecipientOrSenderOrderBySendDate(User user, Pageable pageable);
}
