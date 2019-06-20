package com.projecty.projectyweb.service.message;

import com.projecty.projectyweb.model.Message;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.MessageRepository;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Locale;

import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_SUCCESS;


@Service
public class MessageServiceImpl implements MessageService {
    private static UserService userService;
    private static MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;


    public MessageServiceImpl(UserService userService, MessageRepository messageRepository, UserRepository userRepository, MessageSource messageSource) {
        MessageServiceImpl.userService = userService;
        MessageServiceImpl.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    public static int getMessageCountForCurrentUser() {
        User current = userService.getCurrentUser();
        return messageRepository.findByRecipientAndSeenDateIsNull(current).size();
    }

    @Override
    public boolean checkIfCurrentUserHasPermissionToView(Message message) {
        User current = userService.getCurrentUser();
        return message.getRecipient().equals(current) || message.getSender().equals(current);
    }

    @Override
    public void updateSeenDate(Message message) {
        User current = userService.getCurrentUser();
        if (message.getRecipient() == current && message.getSeenDate() == null) {
            message.setSeenDate(new Timestamp(System.currentTimeMillis()));
            messageRepository.save(message);
        }
    }

    @Override
    public void sendMessage(String recipientUsername, Message message, BindingResult bindingResult) {
        User recipient = userRepository.findByUsername(recipientUsername);
        User sender = userService.getCurrentUser();
        if (recipient == null) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.invalid", null, Locale.getDefault()));
            bindingResult.addError(objectError);
        } else if (sender == recipient) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.yourself", null, Locale.getDefault()));
            bindingResult.addError(objectError);
        } else {
            message.setSender(sender);
            message.setRecipient(recipient);
            messageRepository.save(message);
        }
    }

}
