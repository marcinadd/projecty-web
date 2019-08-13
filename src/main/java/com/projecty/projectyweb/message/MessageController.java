package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
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
            @Valid @ModelAttribute Message message,
            BindingResult bindingResult
    ) throws BindException {
            messageService.sendMessage(recipientUsername,message,bindingResult);
            if (bindingResult.hasErrors()) {
                throw new BindException(bindingResult);
            }
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
