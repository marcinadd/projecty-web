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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping(value = "addProjectToTeam", params = "teamId")
    @EditPermission
    public String addProjectToSpecifiedTeamPost(
            @RequestParam Long teamId
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        return optionalTeam.get().getName();
    }

    @PostMapping("addProjectToTeam")
    public void addProjectToTeamPost(
            @Valid @ModelAttribute Project project,
            @RequestParam Long teamId,
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

    @GetMapping("manageTeam")
    @EditPermission
    public Map<String, Object> manageTeam(
            @RequestParam Long teamId
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Map<String, Object> map = new HashMap<>();
        map.put("team", optionalTeam.get());
        map.put("currentUser", userService.getCurrentUser());
        map.put("teamRoles", optionalTeam.get().getTeamRoles());
        return map;
    }

    @PostMapping("changeName")
    @EditPermission
    public void changeNamePost(
            @RequestParam Long teamId,
            @RequestParam String newName
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamService.changeTeamName(optionalTeam.get(), newName);
    }

    @PostMapping("addUsers")
    @EditPermission
    public void addUsersPost(
            @RequestParam Long teamId,
            @RequestParam(required = false) List<String> usernames
    ) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamRoleService.addTeamMembersByUsernames(optionalTeam.get(), usernames, null);
        teamService.save(optionalTeam.get());
    }

    @PostMapping("deleteTeamRole")
    public void deleteTeamRole(
            @RequestParam Long teamRoleId
    ) {
        // TODO: 6/28/19 Prevent from delete current user from team
        Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
        	teamRoleService.delete(optionalTeamRole.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("changeTeamRole")
    public void changeTeamRolePost(
            @RequestParam Long teamRoleId,
            @RequestParam String newRoleName
    ) {
        Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
            teamRoleService.changeTeamRole(optionalTeamRole.get(), newRoleName);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("projectList")
    @AnyPermission
    public Map<String, Object> projectList(@RequestParam Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("teamName", optionalTeam.get().getName());
        map.put("projects", optionalTeam.get().getProjects());
        map.put("isCurrentUserTeamManager", teamRoleService.isCurrentUserTeamManager(optionalTeam.get()));
        return map;
    }

    @PostMapping("deleteTeam")
    @EditPermission
    public void deleteTeamPost(@RequestParam Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        teamService.delete(optionalTeam.get());
    }

    @PostMapping("leaveTeam")
    @AnyPermission
    public void leaveTeamPost(Long teamId) {
        Optional<Team> optionalTeam = teamService.findById(teamId);
        User current = userService.getCurrentUser();
        teamRoleService.leaveTeam(optionalTeam.get(), current);
    }
}
