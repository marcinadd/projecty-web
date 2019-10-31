package com.projecty.projectyweb.team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectValidator;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;


@CrossOrigin()
@RestController
@RequestMapping("team")
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

    @PostMapping("addTeam")
    public void addTeamPost(
            @ModelAttribute Team team,
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

    @GetMapping("myTeams")
    public List<TeamRole> myTeams() {
        return userService.getCurrentUser().getTeamRoles();
    }

    @GetMapping("addProjectToTeam")
    public List<TeamRole> addProjectToTeam() {
        return teamRoleService.getTeamRolesWhereManager(userService.getCurrentUser());
    }

    @GetMapping(value = "addProjectToTeam/team/{teamId}")
    @EditPermission
    public String addProjectToSpecifiedTeamPost(
            @PathVariable Long teamId
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        return optionalTeam.get().getName();
    }

    @PostMapping("addProjectToTeam/team/{teamId}")
    public void addProjectToTeamPost(
            @Valid @ModelAttribute Project project,
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

    @GetMapping("manageTeam/team/{teamId}")
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

    @PutMapping("changeName/team/{teamId}/into/{name}")
    @EditPermission
    public void changeNamePost(
            @PathVariable Long teamId,
            @PathVariable String name
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamService.changeTeamName(optionalTeam.get(), name);
    }

    @PutMapping("addUsers/{teamId}")
    @EditPermission
    public void addUsersPost(
            @PathVariable Long teamId,
            @RequestParam(required = false) List<String> usernames
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamRoleService.addTeamMembersByUsernames(optionalTeam.get(), usernames, null);
        teamService.save(optionalTeam.get());
    }

    @DeleteMapping("deleteTeamRole/teamRole/{teamRoleId}")
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

    @PutMapping("changeTeamRole/teamRole/{teamRoleId}/to/{roleName}")
    public void changeTeamRolePost(
            @PathVariable Long teamRoleId,
            @PathVariable String roleName
    ) {
        Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
            teamRoleService.changeTeamRole(optionalTeamRole.get(), roleName);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("projectList/{teamId}")
    @AnyPermission
    public Map<String, Object> projectList(@PathVariable Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("teamName", optionalTeam.get().getName());
        map.put("projects", optionalTeam.get().getProjects());
        map.put("isCurrentUserTeamManager", teamRoleService.isCurrentUserTeamManager(optionalTeam.get()));
        return map;
    }

    @DeleteMapping("deleteTeam/{teamId}")
    @EditPermission
    public void deleteTeamPost(@PathVariable Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamService.delete(optionalTeam.get());
    }

    @PutMapping("leaveTeam/{teamId}")
    @AnyPermission
    public void leaveTeamPost(@PathVariable Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        User current = userService.getCurrentUser();
        teamRoleService.leaveTeam(optionalTeam.get(), current);
    }
}
