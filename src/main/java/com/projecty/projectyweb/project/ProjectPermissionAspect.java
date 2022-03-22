package com.projecty.projectyweb.project;

import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoles;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.team.role.TeamRoles;
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
public class ProjectPermissionAspect extends UserServiceField {
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final ProjectRoleRepository projectRoleRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final Logger logger = Logger.getLogger(getClass().getName());

    public ProjectPermissionAspect(UserService userService, ProjectRepository projectRepository, ProjectService projectService,TeamRoleRepository teamRoleRepository, ProjectRoleRepository projectRoleRepository) {
        super(userService);
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.teamRoleRepository = teamRoleRepository;
        this.projectRoleRepository = projectRoleRepository;
    }

    @Pointcut("execution (* com.projecty.projectyweb.project.ProjectController.*(Long,..))" +
            "&&@annotation(com.projecty.projectyweb.configurations.EditPermission)")
    private void inProjectControllerAndWithEditPermission() {
    }

    @Pointcut("execution (* com.projecty.projectyweb.project.ProjectController.*(Long,..))" +
            "&&@annotation(com.projecty.projectyweb.configurations.AnyPermission)")
    private void inProjectControllerAndWithAnyPermission() {
    }

    public boolean hasCurrentUserPermissionToEdit(Project project) {
        User current = userService.getCurrentUser();
        if (project.getTeam() != null) {
            Optional<TeamRole> optionalTeamRole = teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current);
            return optionalTeamRole.isPresent() && optionalTeamRole.get().getName().equals(TeamRoles.MANAGER);
        }
        Optional<ProjectRole> optionalRole = projectRoleRepository.findRoleByUserAndProject(current, project);
        return optionalRole.isPresent() && optionalRole.get().getName().equals(ProjectRoles.ADMIN);
    }

    public boolean hasCurrentUserPermissionToView(Project project) {
        User current = userService.getCurrentUser();
        if (project.getTeam() != null) {
            return teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current).isPresent();
        }
        return hasUserRoleInProject(current, project);
    }

    public boolean hasUserRoleInProject(User user, Project project) {
        return projectRoleRepository.findRoleByUserAndProject(user, project).isPresent();
    }

    @Before("inProjectControllerAndWithEditPermission()")
    public void checkEditPermission(JoinPoint joinPoint) {
        Long projectId = (Long) joinPoint.getArgs()[0];
        User current = userService.getCurrentUser();
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (!(optionalProject.isPresent() && hasCurrentUserPermissionToEdit(optionalProject.get()))) {
            logger.warning("User: "
                    + current.getUsername()
                    + " tried to execute "
                    + joinPoint.getSignature().toString()
                    + " without edit permission");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Before("inProjectControllerAndWithAnyPermission()")
    public void checkAnyPermission(JoinPoint joinPoint) {
        Long projectId = (Long) joinPoint.getArgs()[0];
        User current = userService.getCurrentUser();
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (!(optionalProject.isPresent() && hasCurrentUserPermissionToView(optionalProject.get()))) {
            logger.warning("User: " + current.getUsername()
                    + " tried to execute "
                    + joinPoint.getSignature().toString()
                    + " without any permission");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }


}
