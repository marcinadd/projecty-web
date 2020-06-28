package com.projecty.projectyweb.notifications;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MessageSource messageSource;
    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, MessageSource messageSource, UserService userService, ProjectRepository projectRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.messageSource = messageSource;
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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
                        .seen(false)
                        .build()
        );
    }

    public List<Notification> getNotifications() {
        User user = userService.getCurrentUser();
        List<Notification> allNotifications = notificationRepository.findByUser(user);
        List<Notification> validNotifications = new ArrayList<>();
        allNotifications.forEach(notification -> {
            String stringValue = buildNotificationString(notification);
            if (stringValue != null) {
                notification.setStringValue(stringValue);
                validNotifications.add(notification);
            }
        });
        setNotificationsAsSeen(validNotifications);
        return validNotifications;
    }

    public String buildNotificationString(Notification notification) {
        Map<NotificationObjectType, Long> ids = notification.getIds();
        String msg = null;
        try {
            switch (notification.getNotificationType()) {
                case ADDED_TO_PROJECT:
                    User user = userRepository.findById(ids.get(NotificationObjectType.USER)).get();
                    Project project = projectRepository.findById(ids.get(NotificationObjectType.PROJECT)).get();
                    String[] values = {user.getUsername(), project.getName()};
                    msg = getMessageFromMessageSource(notification.getNotificationType(), values);
                    break;
            }
        } catch (NoSuchElementException e) {
            // Remove orphans
            notificationRepository.delete(notification);
        }
        return msg;
    }

    public String getMessageFromMessageSource(NotificationType type, String[] values) {
        return messageSource.getMessage(convertNotificationType(type), values, Locale.getDefault());
    }

    public String convertNotificationType(NotificationType notificationType) {
        return notificationType.toString().toLowerCase().replaceAll("_", ".");
    }

    public Long getUnseenNotificationCount() {
        return notificationRepository.countByUserAndSeenFalse(userService.getCurrentUser());
    }

    public void setNotificationsAsSeen(List<Notification> notifications) {
        notifications.forEach(notification -> {
            notification.setSeen(true);
            notificationRepository.save(notification);
        });
    }

    public void deleteNotification(Notification notification) {
        notificationRepository.delete(notification);
    }
}
