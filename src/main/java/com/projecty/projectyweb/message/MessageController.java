package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_SUCCESS;

@CrossOrigin()
@RestController
@RequestMapping("message")
public class MessageController {
    private final UserService userService;

    private final MessageRepository messageRepository;

    private final MessageService messageService;

    private final MessageSource messageSource;

    public MessageController(UserService userService, MessageRepository messageRepository, MessageService messageService, MessageSource messageSource) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.messageSource = messageSource;
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

    @GetMapping("sendMessage")
    public ModelAndView sendMessage() {
        return new ModelAndView("fragments/message/send-message", "message", new Message());
    }

    @PostMapping("sendMessage")
    public String sendMessagePost(
            @RequestParam String recipientUsername,
            @Valid @ModelAttribute Message message,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
            messageService.sendMessage(recipientUsername,message,bindingResult);
            if (bindingResult.hasErrors()) {
                return "fragments/message/send-message";
            }
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("message.send.success", null, Locale.getDefault())));
            return "redirect:/messages/messageList";
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
