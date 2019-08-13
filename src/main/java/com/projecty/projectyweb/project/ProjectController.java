package com.projecty.projectyweb.project;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.AppConfig;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoleService;
import com.projecty.projectyweb.project.role.ProjectRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.*;

import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES;
import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_SUCCESS;

@CrossOrigin()
@RestController
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

    @PostMapping("addproject")
    public void addProjectPost(
            @Valid @ModelAttribute Project project,
            @RequestParam(required = false) List<String> usernames,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) throws BindException {
        projectValidator.validate(project, bindingResult);
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            throw new BindException(bindingResult);
        }

        List<RedirectMessage> redirectMessages = new ArrayList<>();
        projectService.createNewProjectAndSave(project, usernames, redirectMessages);
        redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES, redirectMessages);
        redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("project.add.success", null, Locale.getDefault())));
    }

    @PostMapping("deleteProject")
    @EditPermission
    public void deleteProject(@RequestParam Long projectId, RedirectAttributes redirectAttributes) {
        Optional<Project> project = projectRepository.findById(projectId);
        projectRepository.delete(project.get());
        redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("project.delete.success", null, Locale.getDefault())));
    }

    @GetMapping("myProjects")
    public Map<String, Object> myProjects() {
        User current = userService.getCurrentUser();
        Map<String, Object> map = new HashMap<>();
        map.put("projectRoles", current.getProjectRoles());
        map.put("teamRoles", current.getTeamRoles());
        return map;
    }

    @PostMapping("addUsers")
    @EditPermission
    public void addUsersToExistingProjectPost(
            @RequestParam Long projectId,
            @RequestParam(required = false) List<String> usernames,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Project project = optionalProject.get();
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        projectRoleService.addRolesToProjectByUsernames(project, usernames, redirectMessages);
        redirectAttributes.addFlashAttribute(AppConfig.REDIRECT_MESSAGES, redirectMessages);
        projectRepository.save(project);
        redirectAttributes.addAttribute("projectId", projectId);
    }

    @PostMapping("deleteUser")
    @EditPermission
    public String deleteUserPost(
            @RequestParam Long projectId,
            @RequestParam Long userId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        User current = userService.getCurrentUser();
        Optional<User> toDeleteOptionalUser = userRepository.findById(userId);
        if (toDeleteOptionalUser.isPresent() && !toDeleteOptionalUser.get().equals(current)) {
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
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("changeRole")
    @EditPermission
    public String changeRolePost(
            @RequestParam Long projectId,
            @RequestParam Long roleId,
            @RequestParam String newRoleName,
            RedirectAttributes redirectAttributes
    ) {
        Optional<ProjectRole> optionalRole = projectRoleRepository.findById(roleId);
        if (optionalRole.isPresent()) {
            ProjectRole projectRole = optionalRole.get();
            projectRole.setName(ProjectRoles.valueOf(newRoleName));
            projectRoleRepository.save(projectRole);
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("projectRole.change.success", new Object[]{projectRole.getUser().getUsername()}, Locale.getDefault())));
            redirectAttributes.addAttribute("projectId", projectId);
            redirectAttributes.addAttribute("roleId", roleId);
            return "redirect:/project/manageProject";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("manageProject")
    @EditPermission
    public Map<String, Object> manageProject(
            @RequestParam Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Map<String, Object> map = new HashMap<>();
        map.put("project", optionalProject.get());
        map.put("projectRoles", optionalProject.get().getProjectRoles());
        map.put("currentUser", userService.getCurrentUser());
        return map;
    }

    @PostMapping("leaveProject")
    @AnyPermission
    public void leaveProject(Long projectId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        User current = userService.getCurrentUser();
        projectRoleService.leaveProject(optionalProject.get(), current);
    }

    @PostMapping("changeName")
    public void changeNamePost(
            @ModelAttribute("project") Project newProject,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        projectValidator.validate(newProject, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Optional<Project> optionalProject = projectRepository.findById(newProject.getId());
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            Project existingProject = optionalProject.get();
            projectService.changeName(existingProject, newProject.getName());
            redirectAttributes.addAttribute("projectId", existingProject.getId());
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
