package com.projecty.projectyweb.message;

import com.projecty.projectyweb.message.association.AssociationService;
import com.projecty.projectyweb.message.attachment.AttachmentService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class MessageService {
    private final UserService userService;
    private final AssociationService associationService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final AttachmentService attachmentService;
    private final MessageValidator messageValidator;

    public MessageService(UserService userService, MessageRepository messageRepository, UserRepository userRepository, MessageSource messageSource, AttachmentService attachmentService, AssociationService associationService, MessageValidator messageValidator) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.attachmentService = attachmentService;
        this.associationService = associationService;
        this.messageValidator = messageValidator;
    }

    public int getUnreadMessageCountForCurrentUser() {
        User current = userService.getCurrentUser();
        return (int) messageRepository.findByRecipientAndSeenDateIsNull(current).stream()
                .filter(message -> this.associationService.isVisibleForUser(message, current))
                .count();
    }

    public boolean checkIfCurrentUserHasPermissionToView(Message message) {
        return this.associationService.isVisibleForUser(message, userService.getCurrentUser());
    }

    public void updateSeenDate(Message message) {
        User current = userService.getCurrentUser();
        if (message.getRecipient() == current && message.getSeenDate() == null) {
            message.setSeenDate(new Timestamp(System.currentTimeMillis()));
            messageRepository.save(message);
        }
    }

    public Page<Message> getPageOfMessagesForCurrentUser(MessageType messageType, int page, int itemsPerPage) {
        User user = userService.getCurrentUser();
        switch (messageType) {
            case SENT:
                return messageRepository.findBySenderOrderBySendDateDesc(user, PageRequest.of(page, itemsPerPage));
            case RECEIVED:
            default:
                return messageRepository.findByRecipientOrderBySendDateDesc(user, PageRequest.of(page, itemsPerPage));
        }
    }

    public Message sendMessage(
            String recipientUsername,
            Message message,
            BindingResult bindingResult,
            List<MultipartFile> multipartFiles
    ) throws BindException {

        messageValidator.validate(message, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        User recipient = userRepository.findByUsername(recipientUsername).get();
        User sender = userService.getCurrentUser();

        message.setSender(sender);
        message.setRecipient(recipient);
        if (multipartFiles != null) {
            attachmentService.addFilesToMessage(multipartFiles, message);
        }
        Message saved = messageRepository.save(message);
        associationService.recordMessage(message);
        return saved;
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

    public void deleteMessage(Message message) {
        User currentUser = userService.getCurrentUser();
        associationService.deleteMessageForUser(message, currentUser);
        if (Objects.isNull(message.getSender()) && Objects.isNull(message.getRecipient())) {
            messageRepository.delete(message);
        }
    }
}
