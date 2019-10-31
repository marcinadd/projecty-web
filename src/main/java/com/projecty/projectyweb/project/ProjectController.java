package com.projecty.projectyweb.project;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.role.ProjectRoleService;
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
@RequestMapping("projects")
public class ProjectController {
    private final ProjectService projectService;

    private final ProjectRepository projectRepository;

    private final UserService userService;

    private final ProjectValidator projectValidator;

    private final ProjectRoleService projectRoleService;

    public ProjectController(ProjectService projectService, ProjectRepository projectRepository, UserService userService, ProjectValidator projectValidator, ProjectRoleService projectRoleService) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.projectValidator = projectValidator;
        this.projectRoleService = projectRoleService;
    }

    @GetMapping("")
    public Map<String, Object> myProjects() {
        User current = userService.getCurrentUser();
        Map<String, Object> map = new HashMap<>();
        map.put("projectRoles", current.getProjectRoles());
        map.put("teamRoles", current.getTeamRoles());
        return map;
    }

    @PostMapping("")
    public void addProjectPost(
            @Valid @RequestBody Project project,
            BindingResult bindingResult
    ) throws BindException {
        projectValidator.validate(project, bindingResult);
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            throw new BindException(bindingResult);
        }
        System.out.println(project.getUsernames());
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        projectService.createNewProjectAndSave(project, project.getUsernames(), redirectMessages);
    }

    @DeleteMapping("/{projectId}")
    @EditPermission
    public void deleteProject(@PathVariable Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        projectRepository.delete(project.get());
    }

    @PostMapping("/{projectId}/roles")
    @EditPermission
    public void addUsersToExistingProjectPost(
            @PathVariable Long projectId,
            @RequestBody List<String> usernames) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Project project = optionalProject.get();
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        projectRoleService.addRolesToProjectByUsernames(project, usernames, redirectMessages);
        projectRepository.save(project);
    }

    @GetMapping(value = "/{projectId}", params = "roles")
    @EditPermission
    public Map<String, Object> manageProject(
            @PathVariable Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Map<String, Object> map = new HashMap<>();
        map.put("project", optionalProject.get());
        map.put("projectRoles", optionalProject.get().getProjectRoles());
        map.put("currentUser", userService.getCurrentUser());
        return map;
    }

    @PostMapping("/{projectId}/leave")
    @AnyPermission
    public void leaveProject(@PathVariable Long projectId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        User current = userService.getCurrentUser();
        projectRoleService.leaveProject(optionalProject.get(), current);
    }

    @PatchMapping("/{projectId}")
    public void changeNamePost(
            @PathVariable("projectId") Long projectId,
            @RequestBody Map<String, String> fields
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        String name = fields.get("name");
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get()) && !name.isEmpty()) {
            projectService.changeName(optionalProject.get(), name.trim());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{projectId}")
    public Project getProjectData(
            @PathVariable Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            return optionalProject.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
