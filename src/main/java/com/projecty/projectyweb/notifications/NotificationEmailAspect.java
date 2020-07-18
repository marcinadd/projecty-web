package com.projecty.projectyweb.notifications;

import com.projecty.projectyweb.email.EmailService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Profile({"docker", "development"})
public class NotificationEmailAspect {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailAspect.class);
    private final NotificationService notificationService;
    private final EmailService emailService;

    public NotificationEmailAspect(NotificationService notificationService, EmailService emailService) {
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @AfterReturning(value = "execution (* com.projecty.projectyweb.notifications.NotificationService.createNotificationAndSave(..))", returning = "notification")
    @Async
    public void sendEmailNotificationToSpecificUser(Notification notification) {
        if (notification.getUser().getSettings().getIsEmailNotificationEnabled()) {
            notification.setStringValue(notificationService.buildNotificationString(notification));
            Map<String, Object> values = new HashMap<>();
            values.put("text", notification.getStringValue());
            try {
                emailService.sendMessageThymeleafTemplate(notification.getUser().getEmail(), notification.getStringValue(), values);
            } catch (Exception e) {
                logger.warn(String.valueOf(e));
            }
        }
    }
}
