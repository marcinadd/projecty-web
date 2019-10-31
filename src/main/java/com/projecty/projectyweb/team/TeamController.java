package com.projecty.projectyweb.team;

import java.util.ArrayList;
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


@CrossOrigin()
@RestController
@RequestMapping("team")
public class TeamController {
    private final TeamValidator teamValidator;
    private final TeamService teamService;
    private final ProjectValidator projectValidator;

    public TeamController(TeamValidator teamValidator, TeamService teamService, ProjectValidator projectValidator) {
        this.teamValidator = teamValidator;
        this.teamService = teamService;
        this.projectValidator = projectValidator;
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
    	return teamService.listMyTeam();
    }

    @GetMapping("addProjectToTeam")
    public List<TeamRole> addProjectToTeam() {
    	return teamService.addProjectToTeam();
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
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        if(!teamService.addProjectToTeamPost(teamId, project)) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("manageTeam")
    @EditPermission
    public Map<String, Object> manageTeam(
            @RequestParam Long teamId
    ) {
        return teamService.manageTeam(teamId);
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
        teamService.savePost(teamId, usernames);
    }

    @PostMapping("deleteTeamRole")
    public void deleteTeamRole(
            @RequestParam Long teamRoleId
    ) {
        if(!teamService.deleteTeamRole(teamRoleId)) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("changeTeamRole")
    public void changeTeamRolePost(
            @RequestParam Long teamRoleId,
            @RequestParam String newRoleName
    ) {
    	if(!teamService.changeTeamRole(teamRoleId, newRoleName)) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    	}
    }

    @GetMapping("projectList")
    @AnyPermission
    public Map<String, Object> projectList(@RequestParam Long teamId) {
        return teamService.findProjects(teamId);
    }

    @PostMapping("deleteTeam")
    @EditPermission
    public void deleteTeamPost(@RequestParam Long teamId) {
        teamService.delete(teamId);
    }

    @PostMapping("leaveTeam")
    @AnyPermission
    public void leaveTeamPost(Long teamId) {
    	teamService.leaveTeam(teamId);
    }
}
