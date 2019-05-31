package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Message;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.MessageRepository;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.List;

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
    public ModelAndView messageList() {
        User user = userService.getCurrentUser();
        List<Message> messageList = messageRepository.findByRecipient(user);
        return new ModelAndView("fragments/messagelist", "messages", messageList);
    }

    @GetMapping("sendMessage")
    public ModelAndView sendMessage() {
        return new ModelAndView("fragments/sendmessage", "message", new Message());
    }

    @PostMapping("sendMessage")
    public ModelAndView sendMessagePost(
            @Valid @ModelAttribute Message message,
            @RequestParam String recipientUsername,
            BindingResult bindingResult
    ) {
        User recipient = userRepository.findByUsername(recipientUsername);
        User sender = userService.getCurrentUser();

        if (recipient == null) {
            // TODO Remove hardcored text here
            ObjectError objectError = new ObjectError("recipient", "Invalid recipient username");
            bindingResult.addError(objectError);
            return new ModelAndView("fragments/sendmessage");
        } else {
            message.setSender(sender);
            message.setRecipient(recipient);
            messageRepository.save(message);
        }
        return new ModelAndView("redirect:/messages/messageList");
    }


}
