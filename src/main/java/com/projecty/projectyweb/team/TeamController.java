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
            @RequestParam(required = false) List<String> usernames,
            BindingResult bindingResult
    ) throws BindException {
        teamValidator.validate(team, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        teamService.createTeamAndSave(team, usernames, redirectMessages);
    }

    @GetMapping("")
    public List<TeamRole> myTeams() {
        return userService.getCurrentUser().getTeamRoles();
    }

    @GetMapping("addProjectToTeam")
    public List<TeamRole> addProjectToTeam() {
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

    @PostMapping("/{teamId}/project")
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
    public Map<String, Object> manageTeam(
            @PathVariable Long teamId
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Map<String, Object> map = new HashMap<>();
        map.put("team", optionalTeam.get());
        map.put("currentUser", userService.getCurrentUser());
        map.put("teamRoles", optionalTeam.get().getTeamRoles());
        return map;
    }

    @PatchMapping("/{teamId}")
    @EditPermission
    public void changeNamePost(
            @PathVariable Long teamId,
            @RequestBody Map<String, String> fields
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamService.changeTeamName(optionalTeam.get(), fields.get("name"));
    }

    @PostMapping("/{teamId}/roles")
    @EditPermission
    public void addUsersPost(
            @PathVariable Long teamId,
            @RequestBody List<String> usernames
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamRoleService.addTeamMembersByUsernames(optionalTeam.get(), usernames, null);
        teamService.save(optionalTeam.get());
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
