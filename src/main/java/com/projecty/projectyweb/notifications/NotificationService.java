package com.projecty.projectyweb.notifications;

import com.projecty.projectyweb.user.User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotificationAndSave(
            User user,
            NotificationType notificationType,
            Map<NotificationObjectType, Long> map) {
        return notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .notificationType(notificationType)
                        .ids(map)
                        .build()
        );
    }
}
