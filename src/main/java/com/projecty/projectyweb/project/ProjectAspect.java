package com.projecty.projectyweb.project;

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
public class ProjectAspect {
    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private Logger logger = Logger.getLogger(getClass().getName());

    public ProjectAspect(UserService userService, ProjectRepository projectRepository, ProjectService projectService) {
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    @Pointcut("execution (* com.projecty.projectyweb.project.ProjectController.*(Long,..))&&@annotation(com.projecty.projectyweb.configurations.EditPermission)")
    private void inProjectControllerAndWithEditPermission() {
    }

    @Before("inProjectControllerAndWithEditPermission()")
    public void checkPermissions(JoinPoint joinPoint) {
        Long projectId = (Long) joinPoint.getArgs()[0];
        User current = userService.getCurrentUser();
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (!(optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get()))) {
            logger.warning("User: " + current.getUsername() + " tried to execute " + joinPoint.getSignature().toString() + " without permissions");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
