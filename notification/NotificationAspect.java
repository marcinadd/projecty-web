package com.projecty.projectyweb.notification;

import java.util.logging.Logger;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.projecty.projectyweb.project.ProjectService;
import com.projecty.projectyweb.user.UserService;

@Aspect
@Component
public class NotificationAspect {
    private final UserService userService;
    private final NotificationRepository notificationRepository;
    private final ProjectService projectService;
    private Logger logger = Logger.getLogger(getClass().getName());

    public NotificationAspect(UserService userService, NotificationRepository notificationRepository, ProjectService projectService) {
        this.userService = userService;
        this.notificationRepository = notificationRepository;
        this.projectService = projectService;
    }

    @Pointcut("execution (* com.projecty.projectyweb.task.TaskController.*(Long,..))" +
            "&&@annotation(com.projecty.projectyweb.configurations.EditPermission)")
    private void inTaskControllerAndWithEditPermission() {
    }

    @Pointcut("execution (* com.projecty.projectyweb.task.TaskController.*(Long,..))" +
            "&&@annotation(com.projecty.projectyweb.configurations.AnyPermission)")
    private void inTaskControllerAndWithAnyPermission() {
    }
}
