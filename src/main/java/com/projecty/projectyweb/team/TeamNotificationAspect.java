package com.projecty.projectyweb.team;

import com.projecty.projectyweb.notifications.NotificationObjectType;
import com.projecty.projectyweb.notifications.NotificationService;
import com.projecty.projectyweb.notifications.NotificationType;
import com.projecty.projectyweb.team.role.TeamRole;
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
public class TeamNotificationAspect {
    private final UserService userService;
    private final NotificationService notificationService;

    public TeamNotificationAspect(UserService userService, UserRepository userRepository, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @AfterReturning(value = "execution (* com.projecty.projectyweb.team.TeamService.createTeamAndSave(..))", returning = "team")
    public void afterNewTeamCreated(Team team) {
        User currentUser = userService.getCurrentUser();
        if (team.getTeamRoles() != null) {
            team.getTeamRoles().forEach(teamRole -> {
                User teamRoleUser = teamRole.getUser();
                if (!teamRoleUser.equals(currentUser)) {
                    createAddedToTeamNotification(currentUser, team, teamRoleUser);
                }
            });
        }
    }

    @AfterReturning(value = "execution (* com.projecty.projectyweb.team.role.TeamRoleService.addTeamRolesByUsernames(..))", returning = "teamRoles")
    public void afterTeamRolesAdded(List<TeamRole> teamRoles) {
        User currentUser = userService.getCurrentUser();
        teamRoles.forEach(teamRole -> {
            if (teamRole != null) {
                createAddedToTeamNotification(currentUser, teamRole.getTeam(), teamRole.getUser());
            }
        });
    }

    public void createAddedToTeamNotification(User currentUser, Team team, User notifiedUser) {
        Map<NotificationObjectType, String> map = new LinkedHashMap<>();
        map.put(NotificationObjectType.USER, String.valueOf(currentUser.getId()));
        map.put(NotificationObjectType.TEAM, String.valueOf(team.getId()));
        notificationService.createNotificationAndSave(notifiedUser, NotificationType.ADDED_TO_TEAM, map);
    }
}
