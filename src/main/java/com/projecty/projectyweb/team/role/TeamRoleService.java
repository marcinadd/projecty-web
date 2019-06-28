package com.projecty.projectyweb.team.role;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeamRoleService {
    private final UserService userService;
    private final TeamRoleRepository teamRoleRepository;

    public TeamRoleService(UserService userService, TeamRoleRepository teamRoleRepository) {
        this.userService = userService;
        this.teamRoleRepository = teamRoleRepository;
    }

    public void addTeamMembersByUsernames(Team team, List<String> usernames, List<RedirectMessage> redirectMessages) {
        List<TeamRole> teamRoles = new ArrayList<>();
        if (usernames != null) {
            System.out.println(usernames);
            Set<User> users = userService.getUserSetByUsernamesWithoutCurrentUser(usernames);
            removeExistingUsersInTeam(users, team);
            for (User user : users
            ) {
                TeamRole teamRole = new TeamRole();
                teamRole.setTeam(team);
                teamRole.setUser(user);
                teamRole.setName(TeamRoles.MEMBER);
                teamRoles.add(teamRole);
            }
        }
        if (team.getTeamRoles() == null) {
            team.setTeamRoles(teamRoles);
        } else if (teamRoles.size() > 0) {
            team.getTeamRoles().addAll(teamRoles);
        }
    }

    public void addCurrentUserAsTeamManager(Team team) {
        User current = userService.getCurrentUser();
        TeamRole teamRole = new TeamRole();
        teamRole.setTeam(team);
        teamRole.setName(TeamRoles.MANAGER);
        teamRole.setUser(current);
        if (team.getTeamRoles() == null) {
            List<TeamRole> teamRoles = new ArrayList<>();
            teamRoles.add(teamRole);
            team.setTeamRoles(teamRoles);
        } else {
            team.getTeamRoles().add(teamRole);
        }
    }

    public boolean isCurrentUserTeamManager(Team team) {
        User user = userService.getCurrentUser();
        Optional<TeamRole> teamRole = teamRoleRepository.findByTeamAndAndUser(team, user);
        return teamRole.map(role -> role.getName().equals(TeamRoles.MANAGER)).orElse(false);
    }

    private Set<User> getTeamRoleUsers(Team team) {
        List<TeamRole> teamRoles = teamRoleRepository.findByTeam(team);
        Set<User> users = new HashSet<>();
        for (TeamRole teamRole : teamRoles
        ) {
            users.add(teamRole.getUser());
        }
        return users;
    }

    private void removeExistingUsersInTeam(Set<User> users, Team team) {
        if (team.getId() != null) {
            Set<User> existingUsers = getTeamRoleUsers(team);
            users.removeAll(existingUsers);
        }
    }

}
