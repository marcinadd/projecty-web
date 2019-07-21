package com.projecty.projectyweb.task;

import com.projecty.projectyweb.project.ProjectService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.logging.Logger;

@Aspect
@Component
public class TaskAspect {
    private final UserService userService;
    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private Logger logger = Logger.getLogger(getClass().getName());

    public TaskAspect(UserService userService, TaskRepository taskRepository, ProjectService projectService) {
        this.userService = userService;
        this.taskRepository = taskRepository;
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

    @Before("inTaskControllerAndWithEditPermission()")
    public void checkEditPermission(JoinPoint joinPoint) {
        Long taskId = (Long) joinPoint.getArgs()[0];
        User current = userService.getCurrentUser();
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!(optionalTask.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalTask.get().getProject()))) {
            logger.warning("User: "
                    + current.getUsername()
                    + " tried to execute "
                    + joinPoint.getSignature().toString()
                    + " edit permission");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Before("inTaskControllerAndWithAnyPermission()")
    public void checkAnyPermission(JoinPoint joinPoint) {
        Long taskId = (Long) joinPoint.getArgs()[0];
        User current = userService.getCurrentUser();
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!(optionalTask.isPresent() && projectService.hasCurrentUserPermissionToView(optionalTask.get().getProject()))) {
            logger.warning("User: " + current.getUsername()
                    + " tried to execute "
                    + joinPoint.getSignature().toString()
                    + " without any permission");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
