package com.projecty.projectyweb.team.role;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.team.TeamRepository;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TeamRoleService {
    private final UserService userService;
    private final TeamRoleRepository teamRoleRepository;
    private final TeamRepository teamRepository;

    public TeamRoleService(UserService userService, TeamRoleRepository teamRoleRepository, TeamRepository teamRepository) {
        this.userService = userService;
        this.teamRoleRepository = teamRoleRepository;
        this.teamRepository = teamRepository;
    }

    public void addTeamMembersByUsernames(Team team, List<String> usernames, List<RedirectMessage> redirectMessages) {
        List<TeamRole> teamRoles = new ArrayList<>();
        if (usernames != null) {
            Set<User> users = userService.getUserSetByUsernamesWithoutCurrentUser(usernames);
            removeExistingUsersInTeamFromSet(users, team);
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

    public boolean hasCurrentUserRoleInTeam(Team team) {
        User user = userService.getCurrentUser();
        return teamRoleRepository.findByTeamAndAndUser(team, user).isPresent();
    }

    public Set<User> getTeamRoleUsers(Team team) {
        List<TeamRole> teamRoles = teamRoleRepository.findByTeam(team);
        Set<User> users = new HashSet<>();
        for (TeamRole teamRole : teamRoles
        ) {
            users.add(teamRole.getUser());
        }
        return users;
    }

    private void removeExistingUsersInTeamFromSet(Set<User> users, Team team) {
        if (team.getId() != null) {
            Set<User> existingUsers = getTeamRoleUsers(team);
            users.removeAll(existingUsers);
        }
    }

    public void changeTeamRole(TeamRole teamRole, String newRoleName) throws IllegalArgumentException {
        teamRole.setName(TeamRoles.valueOf(newRoleName));
        teamRoleRepository.save(teamRole);
    }

    public List<TeamRole> getTeamRolesWhereManager(User user) {
        List<TeamRole> managerTeamRoles = new ArrayList<>();
        for (TeamRole teamRole : user.getTeamRoles()
        ) {
            if (teamRole.getName() == TeamRoles.MANAGER) {
                managerTeamRoles.add(teamRole);
            }
        }
        return managerTeamRoles;
    }

    public void leaveTeam(Team team, User user) throws NoManagersInTeamException {
        Optional<TeamRole> optionalTeamRole = teamRoleRepository.findByTeamAndAndUser(team, user);
        if (optionalTeamRole.isPresent()) {
            team.getTeamRoles().remove(optionalTeamRole.get());
            if (teamRoleRepository.countByTeamAndName(team, TeamRoles.MANAGER) == 0) {
                throw new NoManagersInTeamException();
            }
            teamRepository.save(team);
        }
    }
}
