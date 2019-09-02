package com.projecty.projectyweb.message;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.message.attachment.Attachment;
import com.projecty.projectyweb.message.attachment.AttachmentService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
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
@RequestMapping("message")
public class MessageController {
    private final UserService userService;

    private final MessageRepository messageRepository;

    private final MessageService messageService;

    private final AttachmentService attachmentService;

    public MessageController(UserService userService, MessageRepository messageRepository, MessageService messageService, AttachmentService attachmentService) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.attachmentService = attachmentService;
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
            @RequestParam String recipientUsername,
            @ModelAttribute Message message,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile[] multipartFiles

    ) throws BindException, IOException, SQLException {
        messageService.sendMessage(recipientUsername, message, bindingResult, multipartFiles);
            if (bindingResult.hasErrors()) {
                throw new BindException(bindingResult);
            }
    }

    @GetMapping(value = "downloadFile")
    @AnyPermission
    public @ResponseBody
    byte[] downloadFile(
            @RequestParam Long messageId,
            @RequestParam(required = false, defaultValue = "0") Long fileId,
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

    @GetMapping("viewMessage")
    @AnyPermission
    public Message viewMessage(
            @RequestParam Long messageId
    ) {
        Optional<Message> optMessage = messageRepository.findById(messageId);
        messageService.updateSeenDate(optMessage.get());
        return optMessage.get();
    }

    @GetMapping("getUnreadMessageCount")
    public int getUnreadMessageCount() {
        return messageService.getUnreadMessageCountForCurrentUser();
    }
}
