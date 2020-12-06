package com.projecty.projectyweb.message;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.message.association.AssociationService;
import com.projecty.projectyweb.message.attachment.AttachmentRepository;
import com.projecty.projectyweb.message.attachment.AttachmentService;
import com.projecty.projectyweb.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;


@CrossOrigin()
@RestController
@RequestMapping("messages")
public class MessageController {
    private final UserService userService;
    private final MessageRepository messageRepository;
    private final MessageService messageService;
    private final AssociationService associationService;
    private final AttachmentService attachmentService;
    private final AttachmentRepository attachmentRepository;

    public MessageController(UserService userService, MessageRepository messageRepository, MessageService messageService, AttachmentService attachmentService, AssociationService associationService, AttachmentRepository attachmentRepository) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.attachmentService = attachmentService;
        this.associationService = associationService;
        this.attachmentRepository = attachmentRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Message>> getPageOfMessages(
            @RequestParam(defaultValue = "ALL") MessageType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int itemsPerPage
    ) {
        return new ResponseEntity<>(messageService.getPageOfMessagesForCurrentUser(type, page, itemsPerPage), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Message> sendMessagePost(
            @RequestParam String recipientUsername,
            @RequestParam String title,
            @RequestParam String text,
            @RequestParam(required = false) List<MultipartFile> multipartFiles

    ) throws BindException {
        Message message = Message.builder()
                .recipientUsername(recipientUsername)
                .title(title)
                .text(text)
                .recipientUsername(recipientUsername)
                .build();
        return new ResponseEntity<>(messageService.sendMessage(message, multipartFiles), HttpStatus.OK);
    }

    @GetMapping("/{messageId}")
    @AnyPermission
    public ResponseEntity<Message> viewMessage(
            @PathVariable Long messageId
    ) {
        Optional<Message> optMessage = messageRepository.findById(messageId);
        if(optMessage.isPresent()) {
            final Message message = optMessage.get();
            if (associationService.isVisibleForUser(message, userService.getCurrentUser())) {
                messageService.updateSeenDate(message);
                return ResponseEntity.ok(message);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @GetMapping("getUnreadMessageCount")
    public ResponseEntity<Integer> getUnreadMessageCount() {
        return new ResponseEntity<>(messageService.getUnreadMessageCountForCurrentUser(), HttpStatus.OK);
    }

    @PostMapping("{replyToMessageId}/reply")
    public ResponseEntity<Message> replyToMessage(
            @PathVariable Long replyToMessageId,
            @RequestParam String title,
            @RequestParam String text,
            @RequestParam(required = false) List<MultipartFile> multipartFiles

    ) throws BindException {
        Message message = Message.builder()
                .title(title)
                .text(text)
                .build();
        return new ResponseEntity<>(messageService.reply(replyToMessageId, message, multipartFiles), HttpStatus.OK);
    }

    @DeleteMapping(value = "{id}")
    public void deleteMessage(
            @PathVariable("id") long messageId
    ) {
        Optional<Message> optMessage = messageRepository.findById(messageId);
        optMessage.ifPresent(messageService::deleteMessage);
    }
}
