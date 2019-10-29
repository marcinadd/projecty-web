package com.projecty.projectyweb.notification;

import java.util.Optional;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.project.ProjectService;

@CrossOrigin()
@RestController
@RequestMapping("project/notification")
public class NotificationController {
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final NotificationValidator notificationValidator;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public NotificationController(ProjectRepository projectRepository, ProjectService projectService, NotificationValidator notificationValidator, NotificationRepository notificationRepository, NotificationService notificationService, MessageSource messageSource) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.notificationValidator = notificationValidator;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("getAllNotifications")
    public Project getAllNotifications(
            @RequestParam Long notificationId
    ) {
    	//TODO: Implement this method
    	notificationService.getNotifications(notificationId);
    	return null; 
    }
    
    @PostMapping("deleteAllNotifications")
    @EditPermission
    public void deleteAllNotifications(
            Long notificationId
    ) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        Notification notification = optionalNotification.get();
        notificationService.removeNotification(notification);
    }
}