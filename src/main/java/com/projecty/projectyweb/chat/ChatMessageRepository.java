package com.projecty.projectyweb.chat;

import com.projecty.projectyweb.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query("select m from ChatMessage m where m.sender=?1 or m.recipient=?1 order by m.id desc")
    Page<ChatMessage> findByRecipientOrSenderOrderById(User user, Pageable pageable);

    @Query(value = "SELECT new com.projecty.projectyweb.chat.UsernameLastChatMessageIdDTO(m.sender.username, max (id)) " +
            "FROM ChatMessage m WHERE m.recipient=?1 group by m.sender.username")
    List<UsernameLastChatMessageIdDTO> findMaxMessageIdGroupBySenderUsername(User user);

    @Query(value = "SELECT new com.projecty.projectyweb.chat.UsernameLastChatMessageIdDTO(m.recipient.username, max (id)) " +
            "FROM ChatMessage m WHERE m.sender=?1 group by m.recipient.username")
    List<UsernameLastChatMessageIdDTO> findMaxMessageIdGroupByRecipientUsername(User user);

    @Query("SELECT m FROM ChatMessage m WHERE m.id in :Ids")
    List<ChatMessage> findByIdInIds(@Param("Ids") Set<Long> Ids);
}
