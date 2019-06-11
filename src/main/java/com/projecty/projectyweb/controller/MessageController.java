package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Message;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.MessageRepository;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_SUCCESS;

@Controller
@RequestMapping("messages")
public class MessageController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageSource messageSource;

    public MessageController(UserService userService, MessageRepository messageRepository, UserRepository userRepository, MessageSource messageSource) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageSource = messageSource;
    }

    @GetMapping("messageList")
    public ModelAndView messageList(
            @RequestParam(required = false) String type
    ) {
        ModelAndView modelAndView = new ModelAndView();
        if (type == null || type.equals("received")) {
            User user = userService.getCurrentUser();
            List<Message> messages = messageRepository.findByRecipientOrderBySendDateDesc(user);
            modelAndView.setViewName("fragments/message/received-message-list");
            modelAndView.addObject("messages", messages);
            modelAndView.addObject("type", "received");
            return modelAndView;
        } else if (type.equals("sent")) {
            User user = userService.getCurrentUser();
            List<Message> messages = messageRepository.findBySenderOrderBySendDateDesc(user);
            modelAndView.setViewName("fragments/message/sent-message-list");
            modelAndView.addObject("messages", messages);
            modelAndView.addObject("type", "sent");
            return modelAndView;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("sendMessage")
    public ModelAndView sendMessage() {
        return new ModelAndView("fragments/message/send-message", "message", new Message());
    }

    @PostMapping("sendMessage")
    public ModelAndView sendMessagePost(
            @RequestParam String recipientUsername,
            @Valid @ModelAttribute Message message,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        User recipient = userRepository.findByUsername(recipientUsername);
        User sender = userService.getCurrentUser();
        if (recipient == null) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.invalid", null, Locale.getDefault()));
            bindingResult.addError(objectError);
            return new ModelAndView("fragments/message/send-message");
        } else if (sender == recipient) {
            ObjectError objectError = new ObjectError("recipient", messageSource.getMessage("message.recipient.yourself", null, Locale.getDefault()));
            bindingResult.addError(objectError);
            return new ModelAndView("fragments/message/send-message");
        } else {
            message.setSender(sender);
            message.setRecipient(recipient);
            messageRepository.save(message);
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("message.send.success", null, Locale.getDefault())));
            return new ModelAndView("redirect:/messages/messageList");
        }
    }

    @GetMapping("viewMessage")
    public ModelAndView viewMessage(
            @RequestParam Long messageId
    ) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        User current = userService.getCurrentUser();
        if (optionalMessage.isPresent() && (optionalMessage.get().getRecipient().equals(current) || optionalMessage.get().getSender().equals(current))) {
            Message message = optionalMessage.get();
            if (optionalMessage.get().getRecipient() == current && message.getSeenDate() == null) {
                message.setSeenDate(new Timestamp(System.currentTimeMillis()));
                messageRepository.save(message);
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new ModelAndView("fragments/message/view-message", "message", optionalMessage.get());
    }
}
