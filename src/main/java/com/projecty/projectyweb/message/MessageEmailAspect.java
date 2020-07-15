package com.projecty.projectyweb.message;

import com.projecty.projectyweb.email.EmailService;
import com.projecty.projectyweb.notifications.NotificationEmailAspect;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
public class MessageEmailAspect {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailAspect.class);
    private final MessageSource messageSource;
    private final EmailService emailService;

    public MessageEmailAspect(MessageSource messageSource, EmailService emailService) {
        this.messageSource = messageSource;
        this.emailService = emailService;
    }

    @AfterReturning(value = "execution (* com.projecty.projectyweb.message.MessageService.sendMessage(..))", returning = "message")
    @Async
    public void sendEmailNotificationAboutMessage(Message message) {
        String title = messageSource.getMessage(
                "received.message.title",
                new String[]{message.getSender().getUsername()},
                LocaleContextHolder.getLocale());
        Map<String, Object> values = new HashMap<>();
        values.put("text", messageSource.getMessage(
                "message.text.hidden",
                null,
                LocaleContextHolder.getLocale()));
        try {
            emailService.sendMessageThymeleafTemplate(message.getRecipient().getEmail(), title, values);
        } catch (Exception e) {
            logger.warn(String.valueOf(e));
        }
    }
}
