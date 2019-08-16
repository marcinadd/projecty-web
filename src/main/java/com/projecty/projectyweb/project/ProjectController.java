package com.projecty.projectyweb.project;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoleService;
import com.projecty.projectyweb.project.role.ProjectRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
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
@RequestMapping("project")
public class ProjectController {
    private final ProjectService projectService;

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    private final ProjectRoleRepository projectRoleRepository;

    private final ProjectValidator projectValidator;

    private final ProjectRoleService projectRoleService;

    public ProjectController(ProjectService projectService, ProjectRepository projectRepository, UserRepository userRepository, UserService userService, ProjectRoleRepository projectRoleRepository, ProjectValidator projectValidator, ProjectRoleService projectRoleService) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.projectRoleRepository = projectRoleRepository;
        this.projectValidator = projectValidator;
        this.projectRoleService = projectRoleService;
    }

    @PostMapping("addProject")
    public void addProjectPost(
            @Valid @ModelAttribute Project project,
            @RequestParam(required = false) List<String> usernames,
            BindingResult bindingResult
    ) throws BindException {
        projectValidator.validate(project, bindingResult);
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            throw new BindException(bindingResult);
        }
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        projectService.createNewProjectAndSave(project, usernames, redirectMessages);
    }

    @PostMapping("deleteProject")
    @EditPermission
    public void deleteProject(@RequestParam Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        projectRepository.delete(project.get());
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
            @RequestParam(required = false) List<String> usernames
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Project project = optionalProject.get();
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        projectRoleService.addRolesToProjectByUsernames(project, usernames, redirectMessages);
        projectRepository.save(project);
    }

    @PostMapping("deleteUser")
    @EditPermission
    public void deleteUserPost(
            @RequestParam Long projectId,
            @RequestParam Long userId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        User current = userService.getCurrentUser();
        Optional<User> toDeleteOptionalUser = userRepository.findById(userId);
        if (toDeleteOptionalUser.isPresent() && !toDeleteOptionalUser.get().equals(current)) {
            User toDeleteUser = toDeleteOptionalUser.get();
            Project project = optionalProject.get();
            projectRoleService.deleteUserFromProject(toDeleteUser, project);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("changeRole")
    @EditPermission
    public void changeRolePost(
            @RequestParam Long projectId,
            @RequestParam Long roleId,
            @RequestParam String newRoleName
    ) {
        Optional<ProjectRole> optionalRole = projectRoleRepository.findById(roleId);
        if (optionalRole.isPresent()) {
            ProjectRole projectRole = optionalRole.get();
            projectRole.setName(ProjectRoles.valueOf(newRoleName));
            projectRoleRepository.save(projectRole);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
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
            BindingResult bindingResult
    ) {
        projectValidator.validate(newProject, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Optional<Project> optionalProject = projectRepository.findById(newProject.getId());
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            Project existingProject = optionalProject.get();
            projectService.changeName(existingProject, newProject.getName());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
