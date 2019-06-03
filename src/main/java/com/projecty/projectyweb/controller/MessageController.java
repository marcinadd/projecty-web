package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Message;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.MessageRepository;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("messages")
public class MessageController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public MessageController(UserService userService, MessageRepository messageRepository, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("messageList")
    public ModelAndView messageList(
            @RequestParam(required = false) String type
    ) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("fragments/messagelist");
        if (type == null || type.equals("received")) {
            User user = userService.getCurrentUser();
            List<Message> messages = messageRepository.findByRecipient(user);
            modelAndView.addObject("messages", messages);
            modelAndView.addObject("type", "received");
            return modelAndView;
        } else if (type.equals("sent")) {
            User user = userService.getCurrentUser();
            List<Message> messages = messageRepository.findBySender(user);
            modelAndView.addObject("messages", messages);
            modelAndView.addObject("type", "sent");
            return modelAndView;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("sendMessage")
    public ModelAndView sendMessage() {
        return new ModelAndView("fragments/sendmessage", "message", new Message());
    }

    @PostMapping("sendMessage")
    public ModelAndView sendMessagePost(
            @RequestParam String recipientUsername,
            @Valid @ModelAttribute Message message,
            BindingResult bindingResult
    ) {
        User recipient = userRepository.findByUsername(recipientUsername);
        User sender = userService.getCurrentUser();
        if (recipient == null) {
            // TODO Remove hardcored text here
            ObjectError objectError = new ObjectError("recipient", "Invalid recipient username");
            bindingResult.addError(objectError);
            return new ModelAndView("fragments/sendmessage");
        } else if (sender == recipient) {
            // TODO Remove hardcored text here
            ObjectError objectError = new ObjectError("recipient", "You cannot send message to yourself");
            bindingResult.addError(objectError);
            return new ModelAndView("fragments/sendmessage");
        } else {
            message.setSender(sender);
            message.setRecipient(recipient);
            messageRepository.save(message);
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
        return new ModelAndView("fragments/viewmessage", "message", optionalMessage.get());
    }
}
