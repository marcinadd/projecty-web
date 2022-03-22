package com.projecty.projectyweb.project;

import com.projecty.projectyweb.notifications.NotificationObjectType;
import com.projecty.projectyweb.notifications.NotificationService;
import com.projecty.projectyweb.notifications.NotificationType;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
public class ProjectNotificationAspect extends UserServiceField{
    private final NotificationService notificationService;

    public ProjectNotificationAspect(UserService userService, UserRepository userRepository, NotificationService notificationService) {
        super(userService);
        this.notificationService = notificationService;
    }

    @AfterReturning(value = "execution (* com.projecty.projectyweb.project.ProjectController.addProjectPost(..))", returning = "project")
    public void afterNewProjectCreated(Project project) {
        User currentUser = userService.getCurrentUser();
        if (project.getProjectRoles() != null) {
            project.getProjectRoles().forEach(projectRole -> {
                User projectRoleUser = projectRole.getUser();
                if (!projectRoleUser.equals(currentUser)) {
                    createAddedToProjectNotification(currentUser, project, projectRoleUser);
                }
            });
        }
    }

    @AfterReturning(value = "execution (* com.projecty.projectyweb.project.ProjectController.addUsersToExistingProjectPost(..))", returning = "projectRoles")
    public void afterProjectRolesAdded(List<ProjectRole> projectRoles) {
        User currentUser = userService.getCurrentUser();
        projectRoles.forEach(projectRole -> {
            createAddedToProjectNotification(currentUser, projectRole.getProject(), projectRole.getUser());
        });
    }

    public void createAddedToProjectNotification(User currentUser, Project project, User notifiedUser) {
        Map<NotificationObjectType, String> map = new LinkedHashMap<>();
        map.put(NotificationObjectType.USER, String.valueOf(currentUser.getId()));
        map.put(NotificationObjectType.PROJECT, String.valueOf(project.getId()));
        notificationService.createNotificationAndSave(notifiedUser, NotificationType.ADDED_TO_PROJECT, map);
    }

    @AfterReturning(value = "execution (* com.projecty.projectyweb.project.role.ProjectRoleService.patchProjectRole(..))", returning = "projectRole")
    public void afterProjectRolePatched(ProjectRole projectRole) {
        User currentUser = userService.getCurrentUser();
        if (projectRole != null) {
            Map<NotificationObjectType, String> map = new LinkedHashMap<>();
            map.put(NotificationObjectType.USER, String.valueOf(currentUser.getId()));
            map.put(NotificationObjectType.PROJECT, String.valueOf(projectRole.getProject().getId()));
            map.put(NotificationObjectType.PROJECT_ROLE_NAME, projectRole.getName().toString());
            notificationService.createNotificationAndSave(projectRole.getUser(), NotificationType.CHANGED_PROJECT_ROLE, map);
        }
    }
}
