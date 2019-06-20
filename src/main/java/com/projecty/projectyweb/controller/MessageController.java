package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Message;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.MessageRepository;
import com.projecty.projectyweb.service.message.MessageService;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
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

@Controller
@RequestMapping("messages")
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
    public ModelAndView viewMessage(
            @RequestParam Long messageId
    ) {
        Optional<Message> optMessage = messageRepository.findById(messageId);
        if (optMessage.isPresent() && messageService.checkIfCurrentUserHasPermissionToView(optMessage.get())) {
            messageService.updateSeenDate(optMessage.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return new ModelAndView("fragments/message/view-message", "message", optMessage.get());
    }
}
