package com.projecty.projectyweb.notifications;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.role.Roles;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.team.TeamRepository;
import com.projecty.projectyweb.team.role.TeamRoles;
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
    private final TeamRepository teamRepository;

    public NotificationService(NotificationRepository notificationRepository, MessageSource messageSource, UserService userService, ProjectRepository projectRepository, UserRepository userRepository, TeamRepository teamRepository) {
        this.notificationRepository = notificationRepository;
        this.messageSource = messageSource;
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    public Notification createNotificationAndSave(
            User user,
            NotificationType notificationType,
            Map<NotificationObjectType, String> map) {
        return notificationRepository.save(
                Notification.builder()
                        .user(user)
                        .notificationType(notificationType)
                        .values(map)
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
        Map<NotificationObjectType, String> ids = notification.getValues();
        try {
            String[] values = null;
            switch (notification.getNotificationType()) {
                case ADDED_TO_PROJECT:
                    User user1 = userRepository.findById(Long.parseLong(ids.get(NotificationObjectType.USER))).get();
                    Project project = projectRepository.findById(Long.parseLong(ids.get(NotificationObjectType.PROJECT))).get();
                    values = new String[]{user1.getUsername(), project.getName()};
                    break;
                case ADDED_TO_TEAM:
                    User user2 = userRepository.findById(Long.parseLong(ids.get(NotificationObjectType.USER))).get();
                    Team team = teamRepository.findById(Long.parseLong(ids.get(NotificationObjectType.TEAM))).get();
                    values = new String[]{user2.getUsername(), team.getName()};
                    break;
                case CHANGED_PROJECT_ROLE:
                    User user3 = userRepository.findById(Long.parseLong(ids.get(NotificationObjectType.USER))).get();
                    Project project3 = projectRepository.findById(Long.parseLong(ids.get(NotificationObjectType.PROJECT))).get();
                    Roles projectRole3 = Roles.valueOf(ids.get(NotificationObjectType.PROJECT_ROLE_NAME));
                    values = new String[]{user3.getUsername(), project3.getName(), projectRole3.toString()};
                    break;
                case CHANGED_TEAM_ROLE:
                    User user4 = userRepository.findById(Long.parseLong(ids.get(NotificationObjectType.USER))).get();
                    Team team4 = teamRepository.findById(Long.parseLong(ids.get(NotificationObjectType.TEAM))).get();
                    TeamRoles teamRole4 = TeamRoles.valueOf(ids.get(NotificationObjectType.TEAM_ROLE_NAME));
                    values = new String[]{user4.getUsername(), team4.getName(), teamRole4.toString()};
                    break;
            }
            return getMessageFromMessageSource(notification.getNotificationType(), values);
        } catch (NoSuchElementException e) {
            // Remove orphans
            notificationRepository.delete(notification);
        }
        return null;
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
