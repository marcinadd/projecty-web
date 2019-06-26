package com.projecty.projectyweb.team.role;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserHelper;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeamRoleService {
    private final UserHelper userHelper;
    private final UserRepository userRepository;
    private final UserService userService;
    private final TeamRoleRepository teamRoleRepository;

    public TeamRoleService(UserHelper userHelper, UserRepository userRepository, UserService userService, TeamRoleRepository teamRoleRepository) {
        this.userHelper = userHelper;
        this.userRepository = userRepository;
        this.userService = userService;
        this.teamRoleRepository = teamRoleRepository;
    }


    public void addTeamRolesToTeamByUsernames(Team team, List<String> usernames, List<RedirectMessage> redirectMessages) {
        List<TeamRole> teamRoles = new ArrayList<>();
        if (usernames != null) {
            usernames = userHelper.cleanUsernames(usernames);
            for (String username : usernames
            ) {
                Optional<User> user = userRepository.findByUsername(username);
                if (user.isPresent()) {
                    TeamRole teamRole = new TeamRole();
                    teamRole.setTeam(team);
                    teamRole.setUser(user.get());
                    teamRole.setName(TeamRoles.MEMBER);
                    teamRoles.add(teamRole);
                }
            }
        }
        if (team.getTeamRoles() == null) {
            team.setTeamRoles(teamRoles);
        } else if (teamRoles.size() > 0) {
            team.getTeamRoles().addAll(teamRoles);
        }
    }

    public void addCurrentUserToTeamAsManager(Team team) {
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

}
