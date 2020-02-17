package com.projecty.projectyweb.message;

import com.projecty.projectyweb.message.association.AssociationService;
import com.projecty.projectyweb.message.attachment.AttachmentService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Validated
@Service
public class MessageService {
    private final UserService userService;
    private final AssociationService associationService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final AttachmentService attachmentService;

    public MessageService(UserService userService, MessageRepository messageRepository, UserRepository userRepository, MessageSource messageSource, AttachmentService attachmentService,AssociationService associationService) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.attachmentService = attachmentService;
        this.associationService = associationService;
    }

    public int getUnreadMessageCountForCurrentUser() {
        User current = userService.getCurrentUser();
        return (int) messageRepository.findByRecipientAndSeenDateIsNull(current).stream()
                .filter(message -> this.associationService.isVisibleForUser(message, current))
                .count();
    }

    public boolean checkIfCurrentUserHasPermissionToView(Message message) {
        return this.associationService.isVisibleForUser(message,userService.getCurrentUser());
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
            @ValidMessage Message message,
            BindingResult bindingResult,
            List<MultipartFile> multipartFiles
    ) throws BindException {
        Optional<User> recipient = userRepository.findByUsername(recipientUsername);
        User sender = userService.getCurrentUser();

        if (!recipient.isPresent()) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.invalid", null, LocaleContextHolder.getLocale()));
            bindingResult.addError(objectError);
        } else if (sender == recipient.get()) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.yourself", null, LocaleContextHolder.getLocale()));
            bindingResult.addError(objectError);
        }
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        message.setSender(sender);
        message.setRecipient(recipient.get());
        if (multipartFiles != null) {
            attachmentService.addFilesToMessage(multipartFiles, message);
        }
        messageRepository.save(message);
        associationService.recordMessage(message);
    }


    public void reply(Long replyToMessageId,
                      Message message,
                      BindingResult bindingResult,
                      List<MultipartFile> multipartFiles) throws BindException {
        Optional<Message> replyToMessage = messageRepository.findById(replyToMessageId);
        if (replyToMessage.isPresent()) {
            message.setReplyToMessage(replyToMessage.get());
            sendMessage(
                    replyToMessage.get().getSender().getUsername(),
                    message,
                    bindingResult,
                    multipartFiles);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public void deleteMessage(Message message){
        User currentUser = userService.getCurrentUser();
        associationService.deleteMessageForUser(message,currentUser);
        if(Objects.isNull(message.getSender())&& Objects.isNull(message.getRecipient())){
           messageRepository.delete(message);
        }
    }
}
