package com.projecty.projectyweb.chat;

import com.projecty.projectyweb.chat.socket.SocketChatMessage;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserNotFoundException;
import com.projecty.projectyweb.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

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

    Page<ChatMessage> findFirstByRecipientOrSenderOrderBySendDate(User recipient, int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        return chatMessageRepository.findFirstByRecipientOrSenderOrderBySendDate(recipient, pageable);
    }

}
