package com.projecty.projectyweb.team;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectValidator;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.*;


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
    public void addTeamPost(
            @RequestBody Team team,
            BindingResult bindingResult
    ) throws BindException {
        teamValidator.validate(team, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        teamService.createTeamAndSave(team, team.getUsernames());
    }

    @GetMapping("")
    public List<TeamRole> myTeams() {
        return userService.getCurrentUser().getTeamRoles();
    }

    @GetMapping(value = "", params = "manager")
    public List<TeamRole> getTeamRolesWhereManager() {
        return teamRoleService.getTeamRolesWhereManager(userService.getCurrentUser());
    }

    @GetMapping("/{teamId}")
    @EditPermission
    public String addProjectToSpecifiedTeamPost(
            @PathVariable Long teamId
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        return optionalTeam.get().getName();
    }

    @PostMapping("/{teamId}/projects")
    public void addProjectToTeamPost(
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
            teamService.createProjectForTeam(optionalTeam.get(), project);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/{teamId}", params = "roles")
    @EditPermission
    public Map<String, Object> getTeamWithRoles(
            @PathVariable Long teamId
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Map<String, Object> map = new HashMap<>();
        Team team = optionalTeam.get();
        team.setProjects(null);
        map.put("team", team);
        map.put("currentUser", userService.getCurrentUser());
        List<TeamRole> teamRoles = team.getTeamRoles();
        teamRoles.forEach(teamRole -> teamRole.setTeam(null));
        map.put("teamRoles", teamRoles);
        return map;
    }

    @PatchMapping("/{teamId}")
    @EditPermission
    public void changeNamePatch(
            @PathVariable Long teamId,
            @RequestBody Map<String, String> fields
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamService.editTeam(optionalTeam.get(), fields);
    }

    @PostMapping("/{teamId}/roles")
    @EditPermission
    public List<TeamRole> addUsersPost(
            @PathVariable Long teamId,
            @RequestBody List<String> usernames
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        return teamRoleService.addTeamMembersByUsernames(optionalTeam.get(), usernames);
    }

    @GetMapping("/{teamId}/projects")
    @AnyPermission
    public Map<String, Object> projectList(@PathVariable Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("teamName", optionalTeam.get().getName());
        map.put("projects", optionalTeam.get().getProjects());
        map.put("isCurrentUserTeamManager", teamRoleService.isCurrentUserTeamManager(optionalTeam.get()));
        return map;
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
}
