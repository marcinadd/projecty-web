package com.projecty.projectyweb.chat;

import com.projecty.projectyweb.chat.socket.SocketChatMessage;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserNotFoundException;
import com.projecty.projectyweb.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final UserService userService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(UserService userService, ChatMessageRepository chatMessageRepository) {
        this.userService = userService;
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage saveInDatabase(SocketChatMessage message) {
        User currentUser = userService.getCurrentUser();
        Optional<User> recipient = userService.findByByUsername(message.getTo());
        if (recipient.isPresent() && recipient.get() != currentUser) {
            ChatMessage chatMessage =
                    new ChatMessage(currentUser, recipient.get(), message.getText(), new Date());
            return save(chatMessage);
        }
        throw new UserNotFoundException();
    }

    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    Page<ChatMessage> findByRecipientAndSenderOrderById(User recipient, int offset, int limit) {
        User currentUser = userService.getCurrentUser();
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        return chatMessageRepository.findByRecipientAndSenderOrderById(recipient, currentUser, pageable);
    }

    List<ChatMessage> getLastMessagesForDistinctUsers() {
        User currentUser = userService.getCurrentUser();
        List<UsernameLastChatMessageIdDTO> maxSenderIds = chatMessageRepository.findMaxMessageIdGroupBySenderUsername(currentUser);
        List<UsernameLastChatMessageIdDTO> maxRecipientIds = chatMessageRepository.findMaxMessageIdGroupByRecipientUsername(currentUser);
        Set<Long> idSet = getLastMessageIdForEachChatParticipant(maxSenderIds, maxRecipientIds);
        List<ChatMessage> abc = chatMessageRepository.findByIdInIds(idSet);
        return abc;
    }

    private Set<Long> getLastMessageIdForEachChatParticipant(List<UsernameLastChatMessageIdDTO> a,
                                                             List<UsernameLastChatMessageIdDTO> b) {
        Map<String, Long> map =
                a.stream().collect(Collectors.toMap(UsernameLastChatMessageIdDTO::getUsername, UsernameLastChatMessageIdDTO::getLastChatMessageId));
        for (UsernameLastChatMessageIdDTO o : b
        ) {
            String username = o.getUsername();
            Long lastMessageId = o.getLastChatMessageId();
            Long lastMessageIdFromA = map.get(username);
            if (lastMessageIdFromA != null) {
                lastMessageId = Math.max(lastMessageId, lastMessageIdFromA);
            }
            map.put(username, lastMessageId);
        }
        map.remove(userService.getCurrentUser().getUsername());
        return new HashSet<>(map.values());
    }

    void setAllReadForChat(User user) {
        User currentUser = userService.getCurrentUser();
        Date seenDate = new Date();
        List<ChatMessage> messages = chatMessageRepository.findBySenderAndCurrentUserWhereSeenDateIsNull(user, currentUser);
        for (ChatMessage message : messages) {
            message.setSeenDate(seenDate);
            save(message);
        }
    }
}
