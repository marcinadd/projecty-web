package com.projecty.projectyweb.service.message;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.MessageRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.stereotype.Service;


@Service
public class MessageServiceImpl implements MessageService {
    private static UserService userService;
    private static MessageRepository messageRepository;

    public MessageServiceImpl(UserService userService, MessageRepository messageRepository) {
        MessageServiceImpl.userService = userService;
        MessageServiceImpl.messageRepository = messageRepository;
    }

    public static int getMessageCountForCurrentUser() {
        User current = userService.getCurrentUser();
        return messageRepository.findByRecipientAndSeenDateIsNull(current).size();
    }
}
