package com.projecty.projectyweb.team.role;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@CrossOrigin()
@RestController
@RequestMapping("teamRoles")
public class TeamRoleController {
    private final TeamRoleService teamRoleService;

    private final UserService userService;

    public TeamRoleController(TeamRoleService teamRoleService, UserService userService) {
        this.teamRoleService = teamRoleService;
        this.userService = userService;
    }

    @DeleteMapping("/{teamRoleId}")
    public void deleteTeamRole(
            @PathVariable Long teamRoleId
    ) {
        // TODO: 6/28/19 Prevent from delete current user from team
        User current = userService.getCurrentUser();
        Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam()) &&
                !optionalTeamRole.get().getUser().equals(current)) {
            teamRoleService.delete(optionalTeamRole.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{teamRoleId}")
    public TeamRole patchTeamRole(
            @PathVariable Long teamRoleId,
            @RequestBody TeamRole patchedValues
    ) {
        User current = userService.getCurrentUser();
        Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam()) &&
                !optionalTeamRole.get().getUser().equals(current)
        ) {
            return teamRoleService.patchTeamRole(optionalTeamRole.get(), patchedValues);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
