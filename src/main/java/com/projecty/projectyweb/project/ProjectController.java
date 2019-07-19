package com.projecty.projectyweb.project;

import com.projecty.projectyweb.configurations.AppConfig;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.role.*;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.*;

import static com.projecty.projectyweb.configurations.AppConfig.*;

@Controller
@RequestMapping("project")
public class ProjectController {
    private final ProjectService projectService;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    private final ProjectRoleRepository projectRoleRepository;

    private final ProjectValidator projectValidator;

    private final ProjectRoleService projectRoleService;

    private final MessageSource messageSource;

    public ProjectController(ProjectService projectService, ProjectRepository projectRepository, UserRepository userRepository, UserService userService, ProjectRoleRepository projectRoleRepository, ProjectValidator projectValidator, ProjectRoleService projectRoleService, MessageSource messageSource) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.projectRoleRepository = projectRoleRepository;
        this.projectValidator = projectValidator;
        this.projectRoleService = projectRoleService;
        this.messageSource = messageSource;
    }

    @GetMapping("addproject")
    public ModelAndView addProject() {
        return new ModelAndView("fragments/project/add-project", "project", new Project());
    }

    @PostMapping("addproject")
    public String addProjectProcess(
            @Valid @ModelAttribute Project project,
            @RequestParam(required = false) List<String> usernames,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        projectValidator.validate(project, bindingResult);
        if (bindingResult.hasErrors()) {
            return "fragments/project/add-project";
        }
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        projectService.createNewProjectAndSave(project, usernames, redirectMessages);
        redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES, redirectMessages);
        redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("project.add.success", null, Locale.getDefault())));
        return "redirect:/project/myprojects";
    }

    @PostMapping("deleteproject")
    public String deleteProject(@RequestParam Long projectId, RedirectAttributes redirectAttributes) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent() && projectService.hasCurrentUserPermissionToEdit(project.get())) {
            projectRepository.delete(project.get());
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("project.delete.success", null, Locale.getDefault())));
            return "redirect:/project/myprojects/";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("myprojects")
    public ModelAndView myProjects() {
        User current = userService.getCurrentUser();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("fragments/project/my-projects");
        modelAndView.addObject("projectRoles", current.getProjectRoles());
        modelAndView.addObject("teamRoles", current.getTeamRoles());
        return modelAndView;
    }

    @PostMapping("addUsers")
    public String addUsersToExistingProject(
            @RequestParam Long projectId,
            @RequestParam(required = false) List<String> usernames,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            Project project = optionalProject.get();
            List<RedirectMessage> redirectMessages = new ArrayList<>();
            projectRoleService.addRolesToProjectByUsernames(project, usernames, redirectMessages);
            redirectAttributes.addFlashAttribute(AppConfig.REDIRECT_MESSAGES, redirectMessages);
            projectRepository.save(project);
        }
        redirectAttributes.addAttribute("projectId", projectId);
        return "redirect:/project/manageProject";
    }

    @PostMapping("deleteuser")
    public String deleteUser(
            @RequestParam Long projectId,
            @RequestParam Long userId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        User current = userService.getCurrentUser();
        Optional<User> toDeleteOptionalUser = userRepository.findById(userId);
        if (toDeleteOptionalUser.isPresent() && !toDeleteOptionalUser.get().equals(current)
                && optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())
        ) {
            User toDeleteUser = toDeleteOptionalUser.get();
            Project project = optionalProject.get();
            projectRoleService.deleteUserFromProject(toDeleteUser, project);
            redirectAttributes.addAttribute("projectId", projectId);
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS,
                    Collections.singletonList(
                            messageSource.getMessage("projectRole.delete.success",
                                    new Object[]{toDeleteUser.getUsername(), project.getName()},
                                    Locale.getDefault())));
            return "redirect:/project/manageProject";
        } else if (toDeleteOptionalUser.isPresent() && toDeleteOptionalUser.get().equals(current)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("changeRole")
    public String changeRolePost(
            @RequestParam Long projectId,
            @RequestParam Long roleId,
            @RequestParam String newRoleName,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Optional<ProjectRole> optionalRole = projectRoleRepository.findById(roleId);
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get()) && optionalRole.isPresent()) {
            ProjectRole projectRole = optionalRole.get();
            projectRole.setName(ProjectRoles.valueOf(newRoleName));
            projectRoleRepository.save(projectRole);
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("projectRole.change.success", new Object[]{projectRole.getUser().getUsername()}, Locale.getDefault())));
            redirectAttributes.addAttribute("projectId", projectId);
            redirectAttributes.addAttribute("roleId", roleId);
            return "redirect:/project/manageProject";
        } else if (optionalProject.isPresent() && optionalRole.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("manageProject")
    public ModelAndView manageProject(
            @RequestParam Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("/fragments/project/manage-project");
            modelAndView.addObject("project", optionalProject.get());
            modelAndView.addObject("currentUser", userService.getCurrentUser());
            return modelAndView;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("leaveProject")
    public String leaveProject(
            Long projectId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        User current = userService.getCurrentUser();
        if (optionalProject.isPresent() && projectService.hasUserRoleInProject(current, optionalProject.get())) {
            try {
                projectRoleService.leaveProject(optionalProject.get(), current);
            } catch (NoAdminsInProjectException e) {
                redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_FAILED,
                        Collections.singletonList(
                                messageSource.getMessage("project.no_admins.exception",
                                        null,
                                        Locale.getDefault())));
            }
            return "redirect:myprojects";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("changeName")
    public String changeNamePost(
            @ModelAttribute("project") Project newProject,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        projectValidator.validate(newProject, bindingResult);
        if (bindingResult.hasErrors()) {
            return "fragments/project/manage-project";
        }

        Optional<Project> optionalProject = projectRepository.findById(newProject.getId());
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            Project existingProject = optionalProject.get();
            projectService.changeName(existingProject, newProject.getName());
            redirectAttributes.addAttribute("projectId", existingProject.getId());
            return "redirect:manageProject";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
