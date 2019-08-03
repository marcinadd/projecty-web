package com.projecty.projectyweb.team;

import com.projecty.projectyweb.team.role.TeamRoleService;
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
public class TeamAspect {
    private final TeamRepository teamRepository;
    private final TeamRoleService teamRoleService;
    private final UserService userService;
    private Logger logger = Logger.getLogger(getClass().getName());

    public TeamAspect(TeamRepository teamRepository, TeamRoleService teamRoleService, UserService userService) {
        this.teamRepository = teamRepository;
        this.teamRoleService = teamRoleService;
        this.userService = userService;
    }

    @Pointcut("execution (* com.projecty.projectyweb.team.TeamController.*(Long,..))" +
            "&&@annotation(com.projecty.projectyweb.configurations.EditPermission)")
    private void inTeamControllerAndWithEditPermission() {
    }

    @Pointcut("execution (* com.projecty.projectyweb.team.TeamController.*(Long,..))" +
            "&&@annotation(com.projecty.projectyweb.configurations.AnyPermission)")
    private void inTeamControllerAndWithAnyPermission() {
    }

    @Before("inTeamControllerAndWithEditPermission()")
    public void checkEditPermission(JoinPoint joinPoint) {
        Long teamId = (Long) joinPoint.getArgs()[0];
        User current = userService.getCurrentUser();
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (!(optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get()))) {
            logger.warning("User: " + current.getUsername()
                    + " tried to execute " + joinPoint.getSignature().toString()
                    + " without edit permission");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Before("inTeamControllerAndWithAnyPermission()")
    public void checkAnyPermission(JoinPoint joinPoint) {
        Long teamId = (Long) joinPoint.getArgs()[0];
        User current = userService.getCurrentUser();
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (!(optionalTeam.isPresent() && teamRoleService.hasCurrentUserRoleInTeam(optionalTeam.get()))) {
            logger.warning("User: " + current.getUsername()
                    + " tried to execute "
                    + joinPoint.getSignature().toString()
                    + " without any permission");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
