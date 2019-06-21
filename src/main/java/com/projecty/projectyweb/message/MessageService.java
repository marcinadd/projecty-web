package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import java.sql.Timestamp;
import java.util.Locale;
import java.util.Optional;


@Service
public class MessageService {
    private static UserService userService;
    private static MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public MessageService(UserService userService, MessageRepository messageRepository, UserRepository userRepository, MessageSource messageSource) {
        MessageService.userService = userService;
        MessageService.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    public static int getMessageCountForCurrentUser() {
        User current = userService.getCurrentUser();
        return messageRepository.findByRecipientAndSeenDateIsNull(current).size();
    }

    public boolean checkIfCurrentUserHasPermissionToView(Message message) {
        User current = userService.getCurrentUser();
        return message.getRecipient().equals(current) || message.getSender().equals(current);
    }

    public void updateSeenDate(Message message) {
        User current = userService.getCurrentUser();
        if (message.getRecipient() == current && message.getSeenDate() == null) {
            message.setSeenDate(new Timestamp(System.currentTimeMillis()));
            messageRepository.save(message);
        }
    }

    public void sendMessage(String recipientUsername, Message message, BindingResult bindingResult) {
        Optional<User> recipient = userRepository.findByUsername(recipientUsername);
        User sender = userService.getCurrentUser();
        if (!recipient.isPresent()) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.invalid", null, Locale.getDefault()));
            bindingResult.addError(objectError);
        } else if (sender == recipient.get()) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.yourself", null, Locale.getDefault()));
            bindingResult.addError(objectError);
        } else {
            message.setSender(sender);
            message.setRecipient(recipient.get());
            messageRepository.save(message);
        }
    }

}
