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
                return messageRepository.findBySenderAndHasReplyIsFalseOrderBySendDateDesc(user, PageRequest.of(page, itemsPerPage));
            case RECEIVED:
                return messageRepository.findByRecipientAndHasReplyIsFalseOrderBySendDateDesc(user, PageRequest.of(page, itemsPerPage));
            case ANY:
            default:
                return messageRepository.findBySenderOrRecipientAndHasReplyIsFalseOrderBySendDateDesc(user, PageRequest.of(page, itemsPerPage));
        }
    }

    public Message sendMessage(
            Message message,
            List<MultipartFile> multipartFiles
    ) throws BindException {
        BindException bindException = new BindException(message, "message");
        messageValidator.validate(message, bindException);

        if (bindException.hasErrors()) {
            throw bindException;
        }

        User recipient = userRepository.findByUsername(message.getRecipientUsername()).get();
        User sender = userService.getCurrentUser();

        message.setSender(sender);
        message.setRecipient(recipient);
        message.setHasReply(false);
        if (multipartFiles != null) {
            attachmentService.addFilesToMessage(multipartFiles, message);
        }
        Message saved = messageRepository.save(message);
        associationService.recordMessage(message);
        return saved;
    }


    public Message reply(Long replyToMessageId,
                         Message message,
                         List<MultipartFile> multipartFiles) throws BindException {
        User user = userService.getCurrentUser();
        Optional<Message> optionalReplyToMessage = messageRepository.findById(replyToMessageId);
        if (optionalReplyToMessage.isPresent()) {
            return saveReply(user, message, optionalReplyToMessage.get(), multipartFiles);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public Message saveReply(User user,
                             Message message,
                             Message replyToMessage,
                             List<MultipartFile> multipartFiles) throws BindException {
        if (replyToMessage.getSender().equals(user)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        message.setRecipientUsername(replyToMessage.getSender().getUsername());
        message.setReplyTo(replyToMessage);
        replyToMessage.setHasReply(true);
        messageRepository.save(replyToMessage);
        return sendMessage(message, multipartFiles);
    }

    public void deleteMessage(Message message) {
        User currentUser = userService.getCurrentUser();
        associationService.deleteMessageForUser(message, currentUser);
        if (Objects.isNull(message.getSender()) && Objects.isNull(message.getRecipient())) {
            messageRepository.delete(message);
        }
    }
}
