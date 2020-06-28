package com.projecty.projectyweb.notifications;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class NotificationSimpAspect {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public NotificationSimpAspect(SimpMessagingTemplate messagingTemplate, NotificationService notificationService) {
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
    }


    @AfterReturning(value = "execution (* com.projecty.projectyweb.notifications.NotificationService.createNotificationAndSave(..))", returning = "notification")
    public void sendSimpNotificationToSpecificUser(Notification notification) {
        notification.setStringValue(notificationService.buildNotificationString(notification));
        messagingTemplate.convertAndSendToUser(notification.getUser().getUsername(), "/secured/user/queue/specific-user", notification);
    }
}
