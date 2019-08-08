package com.projecty.projectyweb.team;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectValidator;
import com.projecty.projectyweb.team.role.NoManagersInTeamException;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.*;

import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_FAILED;

@CrossOrigin()
@RestController
@RequestMapping("team")
public class TeamController {
    private final TeamValidator teamValidator;
    private final TeamRepository teamRepository;
    private final TeamService teamService;
    private final UserService userService;
    private final ProjectValidator projectValidator;
    private final TeamRoleService teamRoleService;
    private final TeamRoleRepository teamRoleRepository;
    private final MessageSource messageSource;

    public TeamController(TeamValidator teamValidator, TeamRepository teamRepository, UserService userService, TeamService teamService, ProjectValidator projectValidator, TeamRoleService teamRoleService, TeamRoleRepository teamRoleRepository, MessageSource messageSource) {
        this.teamValidator = teamValidator;
        this.teamRepository = teamRepository;
        this.userService = userService;
        this.teamService = teamService;
        this.projectValidator = projectValidator;
        this.teamRoleService = teamRoleService;
        this.teamRoleRepository = teamRoleRepository;
        this.messageSource = messageSource;
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
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        return optionalTeam.get().getName();
    }

    @PostMapping("addProjectToTeam")
    public String addProjectToTeamPost(
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
    @EditPermission
    public Map<String, Object> manageTeam(
            @RequestParam Long teamId
    ) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Map<String, Object> map = new HashMap<>();
        map.put("team", optionalTeam.get());
        map.put("currentUser", userService.getCurrentUser());
        map.put("teamRoles", optionalTeam.get().getTeamRoles());
        return map;
    }

    @PostMapping("changeName")
    @EditPermission
    public String changeNamePost(
            @RequestParam Long teamId,
            @RequestParam String newName,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        teamService.changeTeamName(optionalTeam.get(), newName);
        redirectAttributes.addAttribute("teamId", teamId);
        return "redirect:/team/manageTeam";
    }

    @PostMapping("addUsers")
    @EditPermission
    public String addUsersPost(
            @RequestParam Long teamId,
            @RequestParam(required = false) List<String> usernames,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        teamRoleService.addTeamMembersByUsernames(optionalTeam.get(), usernames, null);
        teamRepository.save(optionalTeam.get());
        redirectAttributes.addAttribute("teamId", teamId);
        return "redirect:/team/manageTeam";
    }

    @PostMapping("deleteTeamRole")
    public String deleteTeamRole(
            @RequestParam Long teamRoleId
    ) {
        // TODO: 6/28/19 Prevent from delete current user from team
        Optional<TeamRole> optionalTeamRole = teamRoleRepository.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
            teamRoleRepository.delete(optionalTeamRole.get());
            return "redirect:/team/manageTeam";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("changeTeamRole")
    public String changeTeamRolePost(
            @RequestParam Long teamRoleId,
            @RequestParam String newRoleName
    ) {
        Optional<TeamRole> optionalTeamRole = teamRoleRepository.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
            teamRoleService.changeTeamRole(optionalTeamRole.get(), newRoleName);
            return "OK";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("projectList")
    @AnyPermission
    public Map<String, Object> projectList(@RequestParam Long teamId) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("teamName", optionalTeam.get().getName());
        map.put("projects", optionalTeam.get().getProjects());
        map.put("isCurrentUserTeamManager", teamRoleService.isCurrentUserTeamManager(optionalTeam.get()));
        return map;
    }

    @GetMapping("deleteTeamConfirm")
    @EditPermission
    public ModelAndView deleteTeamConfirm(@RequestParam Long teamId) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        return new ModelAndView("fragments/team/delete-team-confirm", "team", optionalTeam.get());
    }

    @PostMapping("deleteTeam")
    @EditPermission
    public String deleteTeamPost(@RequestParam Long teamId) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        teamRepository.delete(optionalTeam.get());
        return "redirect:/team/myTeams";
    }

    @GetMapping("leaveTeamConfirm")
    @AnyPermission
    public ModelAndView leaveTeamConfirm(@RequestParam Long teamId) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        return new ModelAndView("fragments/team/leave-team-confirm", "team", optionalTeam.get());
    }

    @PostMapping("leaveTeam")
    @AnyPermission
    public String leaveTeamPost(
            Long teamId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Team> optionalTeam = teamRepository.findById(teamId);
        User current = userService.getCurrentUser();
        try {
            teamRoleService.leaveTeam(optionalTeam.get(), current);
        } catch (NoManagersInTeamException e) {
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_FAILED,
                    Collections.singletonList(
                            messageSource.getMessage("team.no_managers.exception",
                                    null,
                                    Locale.getDefault())));
        }
        return "redirect:/team/myTeams";
    }
}
