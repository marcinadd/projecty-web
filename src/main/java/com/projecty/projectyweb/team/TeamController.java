package com.projecty.projectyweb.team;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectValidator;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("team")
public class TeamController {
    private final TeamValidator teamValidator;
    private final TeamRepository teamRepository;
    private final TeamService teamService;
    private final UserService userService;
    private final ProjectValidator projectValidator;
    private final TeamRoleService teamRoleService;

    public TeamController(TeamValidator teamValidator, TeamRepository teamRepository, UserService userService, TeamService teamService, ProjectValidator projectValidator, TeamRoleService teamRoleService) {
        this.teamValidator = teamValidator;
        this.teamRepository = teamRepository;
        this.userService = userService;
        this.teamService = teamService;
        this.projectValidator = projectValidator;
        this.teamRoleService = teamRoleService;
    }

    @GetMapping("addTeam")
    public ModelAndView addTeam() {
        return new ModelAndView("fragments/team/add-team", "team", new Team());
    }

    @PostMapping("addTeam")
    public String addTeamPost(
            @Valid @ModelAttribute Team team,
            @RequestParam(required = false) List<String> usernames,
            BindingResult bindingResult
    ) {
        teamValidator.validate(team, bindingResult);
        if (bindingResult.hasErrors()) {
            return "fragments/team/add-team";
        }
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        teamService.createTeamAndSave(team, usernames, redirectMessages);
        return "redirect:/team/myTeams";
    }

    @GetMapping("myTeams")
    public ModelAndView myTeams() {
        return new ModelAndView(
                "fragments/team/my-teams",
                "teamRoles",
                userService.getCurrentUser().getTeamRoles());
    }

    @GetMapping("addProjectTeam")
    public ModelAndView addProjectForTeam() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("fragments/team/add-project-team");
        Project project = new Project();
        modelAndView.addObject("project", project);
        modelAndView.addObject("teamRoles", userService.getCurrentUser().getTeamRoles());
        return modelAndView;
    }

    @PostMapping("addProjectTeam")
    public String addProjectTeam(
            @Valid @ModelAttribute Project project,
            @RequestParam Long teamId,
            BindingResult bindingResult
    ) {
        projectValidator.validate(project, bindingResult);
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (bindingResult.hasErrors()) {
            return "fragments/team/add-project-team";
        } else if (optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get())) {
            teamService.createProjectForTeam(optionalTeam.get(), project);
            return "redirect:/team/myTeams";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("manageTeam")
    public ModelAndView manageTeam(
            @RequestParam Long teamId
    ) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get())) {
            return new ModelAndView("fragments/team/manage-team", "team", optionalTeam.get());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("changeName")
    public String changeNamePost(
            @RequestParam Long teamId,
            @RequestParam String newName,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get())) {
            teamService.changeTeamName(optionalTeam.get(), newName);
            redirectAttributes.addAttribute("teamId", teamId);
            return "redirect:/team/myTeams";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

}
