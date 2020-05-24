package com.projecty.projectyweb.project;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.role.ProjectRole;
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
        List<ProjectRole> roles = current.getProjectRoles();
        roles.forEach(role -> projectService.addSummaryToProject(role.getProject()));
        map.put("projectRoles", roles);
        map.put("teamRoles", current.getTeamRoles());
        return map;
    }

    @PostMapping("")
    public Project addProjectPost(
            @Valid @RequestBody Project project,
            BindingResult bindingResult
    ) throws BindException {
        projectValidator.validate(project, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        return projectService.createNewProjectAndSave(project, project.getUsernames(), redirectMessages);
    }

    @DeleteMapping("/{projectId}")
    @EditPermission
    public void deleteProject(@PathVariable Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        projectRepository.delete(project.get());
    }

    @PostMapping("/{projectId}/roles")
    @EditPermission
    public List<ProjectRole> addUsersToExistingProjectPost(
            @PathVariable Long projectId,
            @RequestBody List<String> usernames) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Project project = optionalProject.get();
        List<RedirectMessage> redirectMessages = new ArrayList<>();
        return projectRoleService.addRolesToProjectByUsernames(project, usernames, redirectMessages);
    }

    @GetMapping(value = "/{projectId}", params = "roles")
    @EditPermission
    public Map<String, Object> manageProject(
            @PathVariable Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Map<String, Object> map = new HashMap<>();
        List<ProjectRole> projectRoles = optionalProject.get().getProjectRoles();
        projectRoles.sort(Comparator.comparing(ProjectRole::getId));
        map.put("project", optionalProject.get());
        map.put("projectRoles", projectRoles);
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
    public Project patchProject(
            @PathVariable("projectId") Long projectId,
            @RequestBody Project patchedProject
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            return projectService.patchProject(optionalProject.get(), patchedProject);
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
