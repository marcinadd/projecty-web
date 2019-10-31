package com.projecty.projectyweb.message;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.message.association.AssociationService;
import com.projecty.projectyweb.message.attachment.Attachment;
import com.projecty.projectyweb.message.attachment.AttachmentService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
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

    public MessageController(UserService userService, MessageRepository messageRepository, MessageService messageService, AttachmentService attachmentService , AssociationService associationService) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.attachmentService = attachmentService;
        this.associationService = associationService;
    }

    @GetMapping("receivedMessages")
    public List<Message> receivedMessages() {
        User user = userService.getCurrentUser();
        return messageRepository.findByRecipientOrderBySendDateDesc(user);
    }

    @GetMapping("sentMessages")
    public List<Message> sendMessages() {
        User user = userService.getCurrentUser();
        return messageRepository.findBySenderOrderBySendDateDesc(user);
    }

    @PostMapping("sendMessage")
    public void sendMessagePost(
            @RequestBody Message message,
            BindingResult bindingResult,
            @RequestParam(required = false) List<MultipartFile> multipartFiles

    ) throws BindException {
        messageService.sendMessage(message.getRecipientUsername(), message, bindingResult, multipartFiles);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
    }

    @GetMapping("/{messageId}/files/{fileId}")
    @AnyPermission
    public @ResponseBody
    byte[] downloadFile(
            @PathVariable Long messageId,
            @PathVariable(required = false) Long fileId,
            HttpServletResponse response
    ) throws IOException, SQLException {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        Message message = optionalMessage.get();
        Attachment attachment = message.getAttachments().get(Math.toIntExact(fileId));
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + attachment.getFileName());
        response.flushBuffer();
        return attachmentService.getByteArrayFromAttachment(attachment);
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
    public int getUnreadMessageCount() {
        return messageService.getUnreadMessageCountForCurrentUser();
    }

    @PostMapping("{replyToMessageId}/reply")
    public void reply(
            @PathVariable(name = "replyToMessageId") Long replyToMessageId,
            @ModelAttribute Message message,
            BindingResult bindingResult,
            @RequestParam(required = false) List<MultipartFile> multipartFiles

    ) throws BindException {
        messageService.reply(replyToMessageId, message, bindingResult, multipartFiles);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
    }

    @DeleteMapping(value = "{id}")
    public void deleteMessage(
            @PathVariable("id") long messageId
    ) {
        Optional<Message> optMessage = messageRepository.findById(messageId);
        optMessage.ifPresent(messageService::deleteMessage);
    }

}
