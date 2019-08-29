package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
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

    public MessageController(UserService userService, MessageRepository messageRepository, MessageService messageService) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
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
            @RequestParam(required = false) MultipartFile multipartFile

    ) throws BindException, IOException, SQLException {
        messageService.sendMessage(recipientUsername, message, bindingResult, multipartFile);
            if (bindingResult.hasErrors()) {
                throw new BindException(bindingResult);
            }
    }

    @GetMapping(value = "downloadFile")
    public @ResponseBody
    byte[] downloadFile(
            @RequestParam Long messageId,
            HttpServletResponse response
    ) throws IOException, SQLException {

        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + message.getFileName());
            response.flushBuffer();
            Blob blob = message.getFile();
            InputStream inputStream = blob.getBinaryStream();
            return IOUtils.toByteArray(inputStream);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("viewMessage")
    public Message viewMessage(
            @RequestParam Long messageId
    ) {
        Optional<Message> optMessage = messageRepository.findById(messageId);
        if (optMessage.isPresent() && messageService.checkIfCurrentUserHasPermissionToView(optMessage.get())) {
            messageService.updateSeenDate(optMessage.get());
            return optMessage.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("getUnreadMessageCount")
    public int getUnreadMessageCount() {
        return messageService.getUnreadMessageCountForCurrentUser();
    }
}
