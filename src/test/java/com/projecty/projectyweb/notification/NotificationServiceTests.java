package com.projecty.projectyweb.notification;


import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.notifications.*;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class NotificationServiceTests {
    private static final String PROJECT_NAME = "projectName";
    private static final String USERNAME_B = "notificationServiceUsernameB";
    private static final String USERNAME_C = "notificationServiceUsernameC";
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @Before
    public void init() {

    }

    @Test
    public void whenBuildNotificationString_shouldReturnNotificationString() {
        final String USERNAME = "notificationServiceUsernameA";
        User user = userRepository.save(User.builder().username(USERNAME).build());
        Project project = projectRepository.save(Project.builder().name(PROJECT_NAME).build());
        Map<NotificationObjectType, String> ids = new LinkedHashMap<>();
        ids.put(NotificationObjectType.USER, String.valueOf(user.getId()));
        ids.put(NotificationObjectType.PROJECT, String.valueOf(project.getId()));
        Notification notification = notificationService.createNotificationAndSave(user, NotificationType.ADDED_TO_PROJECT, ids);
        String stringValue = notificationService.buildNotificationString(notification);
        assertThat(stringValue, is("User " + USERNAME + " added you to project " + PROJECT_NAME));
    }

    @Test
    @WithMockUser(USERNAME_B)
    @Transactional
    public void whenGetAllNotifications_shouldReturnAllNotifications() {
        User user = userRepository.save(User.builder().username(USERNAME_B).build());
        Project project = projectRepository.save(Project.builder().name(PROJECT_NAME).build());
        Map<NotificationObjectType, String> ids = new LinkedHashMap<>();
        ids.put(NotificationObjectType.USER, String.valueOf(user.getId()));
        ids.put(NotificationObjectType.PROJECT, String.valueOf(project.getId()));
        Notification notification = notificationService.createNotificationAndSave(user, NotificationType.ADDED_TO_PROJECT, ids);
        assertTrue(notificationService.getNotifications().contains(notification));
    }

    @Test
    @WithMockUser(USERNAME_C)
    public void whenGetUnseenNotificationCount_shouldReturnUnseenNotificationCount() {
        User user = userRepository.save(User.builder().username(USERNAME_C).build());
        notificationRepository.save(Notification.builder().user(user).seen(false).build());
        notificationRepository.save(Notification.builder().user(user).seen(false).build());
        notificationRepository.save(Notification.builder().user(user).seen(false).build());
        assertThat(notificationService.getUnseenNotificationCount(), is(3L));

    }
}
