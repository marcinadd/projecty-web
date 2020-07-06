package com.projecty.projectyweb.team;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectValidator;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.team.role.dto.TeamProjectsData;
import com.projecty.projectyweb.team.role.dto.TeamRoleData;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@CrossOrigin()
@RestController
@RequestMapping("teams")
public class TeamController {
    private final TeamValidator teamValidator;
    private final TeamService teamService;
    private final UserService userService;
    private final ProjectValidator projectValidator;
    private final TeamRoleService teamRoleService;

    public TeamController(TeamValidator teamValidator, UserService userService, TeamService teamService, ProjectValidator projectValidator, TeamRoleService teamRoleService) {
        this.teamValidator = teamValidator;
        this.userService = userService;
        this.teamService = teamService;
        this.projectValidator = projectValidator;
        this.teamRoleService = teamRoleService;
    }

    @PostMapping("")
    public Team addTeam(
            @RequestBody Team team,
            BindingResult bindingResult
    ) throws BindException {
        teamValidator.validate(team, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        return teamService.createTeamAndSave(team, team.getUsernames());
    }

    @GetMapping("")
    public List<TeamRoleData> myTeams() {
        return teamService.getTeams();
    }

    @GetMapping(value = "", params = "manager")
    public List<TeamRole> getTeamRolesWhereManager() {
        return teamRoleService.getTeamRolesWhereManager(userService.getCurrentUser());
    }

    @GetMapping("/{teamId}/name")
    @EditPermission
    public Map<String, String> getTeamName(
            @PathVariable Long teamId
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", optionalTeam.get().getName());
        return map;
    }

    @PostMapping("/{teamId}/projects")
    public Project addProjectToTeamPost(
            @Valid @RequestBody Project project,
            @PathVariable Long teamId,
            BindingResult bindingResult
    ) throws BindException {
        projectValidator.validate(project, bindingResult);
        Optional<Team> optionalTeam = teamService.findById(teamId);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        if (optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get())) {
            return teamService.createProjectForTeam(optionalTeam.get(), project);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{teamId}")
    @EditPermission
    public Team findTeamById(
            @PathVariable Long teamId
    ) {
        return teamService.findById(teamId).get();
    }

    @PatchMapping("/{teamId}")
    @EditPermission
    public Team patchTeam(
            @PathVariable Long teamId,
            @RequestBody Team team
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        return teamService.editTeam(optionalTeam.get(), team);
    }

    @PostMapping("/{teamId}/roles")
    @EditPermission
    public List<TeamRole> addUsersPost(
            @PathVariable Long teamId,
            @RequestBody List<String> usernames
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Team team = optionalTeam.get();
        return teamRoleService.addTeamRolesByUsernames(team, usernames);
    }

    @GetMapping("/{teamId}/projects")
    @AnyPermission
    public TeamProjectsData projectList(@PathVariable Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        return teamService.getTeamProjects(optionalTeam.get());
    }

    @DeleteMapping("/{teamId}")
    @EditPermission
    public void deleteTeamPost(@PathVariable Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamService.delete(optionalTeam.get());
    }

    @PostMapping("/{teamId}/leave")
    @AnyPermission
    public void leaveTeamPost(@PathVariable Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        User current = userService.getCurrentUser();
        teamRoleService.leaveTeam(optionalTeam.get(), current);
    }

    @GetMapping("/{teamId}/teamRole")
    @AnyPermission
    public TeamRoleData getTeamRoleForCurrentUserByTeamId(@PathVariable Long teamId) {
        TeamRoleData teamRoleData = teamService.getTeamRoleForCurrentUserByTeamId(teamId);
        if (teamRoleData != null) {
            return teamRoleData;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
