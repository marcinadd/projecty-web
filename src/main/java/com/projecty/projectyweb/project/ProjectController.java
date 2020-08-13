package com.projecty.projectyweb.project;

import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.project.dto.ProjectData;
import com.projecty.projectyweb.project.dto.ProjectsData;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleService;
import com.projecty.projectyweb.project.role.dto.ProjectRoleData;
import com.projecty.projectyweb.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalGetWithoutIsPresent")
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
    public ProjectsData myProjects() {
        return projectService.getProjectsForCurrentUser();
    }

    @GetMapping("invitations")
    public List<ProjectRole> myInvitations() {
        return projectService.getInvitationsForCurrentUser();
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
        return projectService.createNewProjectAndSave(project, project.getUsernames());
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
        Project project = projectRepository.findById(projectId).get();
        return projectService.addProjectRolesByUsernames(project, usernames);
    }

    @GetMapping(value = "/{projectId}", params = "roles")
    @EditPermission
    public ProjectData getProjectWithProjectRoles(
            @PathVariable Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        return projectService.getProjectData(optionalProject.get());
    }

    @PostMapping("/{projectId}/leave")
    @AnyPermission
    public void leaveProject(@PathVariable Long projectId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        projectRoleService.leaveProject(optionalProject.get());
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

    @GetMapping("/{projectId}/projectRole")
    @AnyPermission
    public ProjectRoleData getProjectRoleForCurrentUserByProjectId(@PathVariable Long projectId) {
        ProjectRoleData projectRoleData = projectService.getProjectRoleForCurrentUserByProjectId(projectId);
        if (projectRoleData != null) {
            return projectRoleData;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
