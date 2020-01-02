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
    @Query("select m from ChatMessage m where (m.sender=?1 and m.recipient=?2) or (m.recipient=?1 and m.sender=?2) order by m.id desc")
    Page<ChatMessage> findByRecipientAndSenderOrderById(User user1, User user2, Pageable pageable);

    @Query(value = "select new com.projecty.projectyweb.chat.UsernameLastChatMessageIdDTO(m.sender.username, max (id)) " +
            "from ChatMessage m where m.recipient=?1 group by m.sender.username")
    List<UsernameLastChatMessageIdDTO> findMaxMessageIdGroupBySenderUsername(User user);

    @Query(value = "select new com.projecty.projectyweb.chat.UsernameLastChatMessageIdDTO(m.recipient.username, max (id)) " +
            "from ChatMessage m where m.sender=?1 group by m.recipient.username")
    List<UsernameLastChatMessageIdDTO> findMaxMessageIdGroupByRecipientUsername(User user);

    @Query("select m from ChatMessage m where m.id in :Ids")
    List<ChatMessage> findByIdInIds(@Param("Ids") Set<Long> Ids);

    @Query("select m from ChatMessage m where m.sender=?1 and m.recipient=?2 and m.seenDate is null ")
    List<ChatMessage> findBySenderAndCurrentUserWhereSeenDateIsNull(User sender, User currentUser);

    @Query(value = "select new com.projecty.projectyweb.chat.UserIdChatMessageCountDTO(m.sender.id, count (id)) " +
            "from ChatMessage m where m.recipient=?1 and m.seenDate is null group by m.sender.id")
    List<UserIdChatMessageCountDTO> countMessagesBySenderWhereSeenDateIsNullGroupBySender(User currentUser);
}
