package com.projecty.projectyweb.team.role;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@CrossOrigin()
@RestController
@RequestMapping("teamRoles")
public class TeamRoleController {
    private final TeamRoleService teamRoleService;

    public TeamRoleController(TeamRoleService teamRoleService) {
        this.teamRoleService = teamRoleService;
    }

    @DeleteMapping("/{teamRoleId}")
    public void deleteTeamRole(
            @PathVariable Long teamRoleId
    ) {
        // TODO: 6/28/19 Prevent from delete current user from team
        Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
            teamRoleService.delete(optionalTeamRole.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{teamRoleId}")
    public void changeTeamRolePost(
            @PathVariable Long teamRoleId,
            @RequestBody Map<String, String> team
    ) {
        Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
            teamRoleService.changeTeamRole(optionalTeamRole.get(), team.get("name"));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
