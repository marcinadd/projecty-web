package com.projecty.projectyweb.team;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectValidator;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
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
    private final TeamRoleRepository teamRoleRepository;

    public TeamController(TeamValidator teamValidator, TeamRepository teamRepository, UserService userService, TeamService teamService, ProjectValidator projectValidator, TeamRoleService teamRoleService, TeamRoleRepository teamRoleRepository) {
        this.teamValidator = teamValidator;
        this.teamRepository = teamRepository;
        this.userService = userService;
        this.teamService = teamService;
        this.projectValidator = projectValidator;
        this.teamRoleService = teamRoleService;
        this.teamRoleRepository = teamRoleRepository;
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
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("fragments/team/manage-team");
            modelAndView.addObject("team", optionalTeam.get());
            modelAndView.addObject("currentUser", userService.getCurrentUser());
            return modelAndView;
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
            return "redirect:/team/manageTeam";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("addUsers")
    public String addUsersPost(
            @RequestParam Long teamId,
            @RequestParam(required = false) List<String> usernames,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        if (optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get())) {
            teamRoleService.addTeamMembersByUsernames(optionalTeam.get(), usernames, null);
            teamRepository.save(optionalTeam.get());
            redirectAttributes.addAttribute("teamId", teamId);
            return "redirect:/team/manageTeam";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("deleteTeamRole")
    public String deleteTeamRole(
            @RequestParam Long teamId,
            @RequestParam Long teamRoleId,
            RedirectAttributes redirectAttributes
    ) {
        // TODO: 6/28/19 Prevent from delete current user from team
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Optional<TeamRole> optionalTeamRole = teamRoleRepository.findById(teamRoleId);
        if (optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get()) && optionalTeamRole.isPresent()) {
            teamRoleRepository.delete(optionalTeamRole.get());
            redirectAttributes.addAttribute("teamId", teamId);
            return "redirect:/team/manageTeam";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("changeTeamRole")
    public String changeTeamRolePost(
            @RequestParam Long teamId,
            @RequestParam Long teamRoleId,
            @RequestParam String newRoleName,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Optional<TeamRole> optionalTeamRole = teamRoleRepository.findById(teamRoleId);
        if (optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get()) && optionalTeamRole.isPresent()) {
            teamRoleService.changeTeamRole(optionalTeamRole.get(), newRoleName);
            redirectAttributes.addAttribute("teamId", teamId);
            return "redirect:/team/manageTeam";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);

    }
}
