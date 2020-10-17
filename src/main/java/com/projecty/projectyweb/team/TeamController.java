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
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Team> addTeam(
            @RequestBody Team team,
            BindingResult bindingResult
    ) throws BindException {
        teamValidator.validate(team, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        return new ResponseEntity<>(teamService.createTeamAndSave(team, team.getUsernames()), HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<List<TeamRoleData>> myTeams() {
        return new ResponseEntity<>(teamService.getTeams(), HttpStatus.OK);
    }

    @GetMapping(value = "", params = "manager")
    public ResponseEntity<List<TeamRole>> getTeamRolesWhereManager() {
        return new ResponseEntity<>(teamRoleService.getTeamRolesWhereManager(userService.getCurrentUser()), HttpStatus.OK);
    }

    @GetMapping("/{teamId}/name")
    @EditPermission
    public ResponseEntity<Map<String, String>> getTeamName(
            @PathVariable Long teamId
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", optionalTeam.get().getName());
        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @PostMapping("/{teamId}/projects")
    public ResponseEntity<Project> addProjectToTeamPost(
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
            return new ResponseEntity<>(teamService.createProjectForTeam(optionalTeam.get(), project), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{teamId}")
    @EditPermission
    public ResponseEntity<Team> findTeamById(
            @PathVariable Long teamId
    ) {
        return new ResponseEntity<>(teamService.findById(teamId).get(), HttpStatus.OK);
    }

    @PatchMapping("/{teamId}")
    @EditPermission
    public ResponseEntity<Team> patchTeam(
            @PathVariable Long teamId,
            @RequestBody Team team
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        return new ResponseEntity<>(teamService.editTeam(optionalTeam.get(), team), HttpStatus.OK);
    }

    @PostMapping("/{teamId}/roles")
    @EditPermission
    public ResponseEntity<List<TeamRole>> addUsersPost(
            @PathVariable Long teamId,
            @RequestBody List<String> usernames
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Team team = optionalTeam.get();
        return new ResponseEntity<>(teamRoleService.addTeamRolesByUsernames(team, usernames), HttpStatus.OK);
    }

    @GetMapping("/{teamId}/projects")
    @AnyPermission
    public ResponseEntity<TeamProjectsData> projectList(@PathVariable Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        return new ResponseEntity<>(teamService.getTeamProjects(optionalTeam.get()), HttpStatus.OK);
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
    public ResponseEntity<TeamRoleData> getTeamRoleForCurrentUserByTeamId(@PathVariable Long teamId) {
        TeamRoleData teamRoleData = teamService.getTeamRoleForCurrentUserByTeamId(teamId);
        if (teamRoleData != null) {
            return new ResponseEntity<>(teamRoleData, HttpStatus.OK);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
