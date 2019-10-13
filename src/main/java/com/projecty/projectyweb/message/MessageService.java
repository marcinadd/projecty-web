package com.projecty.projectyweb.message;

import com.projecty.projectyweb.message.attachment.AttachmentService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;


@Service
public class MessageService {
    private static UserService userService;
    private static MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final AttachmentService attachmentService;

    public MessageService(UserService userService, MessageRepository messageRepository, UserRepository userRepository, MessageSource messageSource, AttachmentService attachmentService) {
        MessageService.userService = userService;
        MessageService.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.attachmentService = attachmentService;
    }

    public int getUnreadMessageCountForCurrentUser() {
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

    public void sendMessage(
            String recipientUsername,
            Message message,
            BindingResult bindingResult,
            List<MultipartFile> multipartFiles
    ) {
        Optional<User> recipient = userRepository.findByUsername(recipientUsername);
        User sender = userService.getCurrentUser();
        if (!recipient.isPresent()) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.invalid", null, LocaleContextHolder.getLocale()));
            bindingResult.addError(objectError);
        } else if (sender == recipient.get()) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.yourself", null, LocaleContextHolder.getLocale()));
            bindingResult.addError(objectError);
        } else {
            message.setSender(sender);
            message.setRecipient(recipient.get());
            if (multipartFiles != null) {
                attachmentService.addFilesToMessage(multipartFiles, message);
            }
            messageRepository.save(message);
        }
    }

}
