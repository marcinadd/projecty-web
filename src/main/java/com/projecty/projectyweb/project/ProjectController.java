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
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ProjectsData> myProjects() {
        return new ResponseEntity<>(projectService.getProjectsForCurrentUser(), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Project> addProjectPost(
            @Valid @RequestBody Project project,
            BindingResult bindingResult
    ) throws BindException {
        projectValidator.validate(project, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        return new ResponseEntity<>(projectService.createNewProjectAndSave(project, project.getUsernames()), HttpStatus.OK);
    }

    @DeleteMapping("/{projectId}")
    @EditPermission
    public void deleteProject(@PathVariable Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        projectRepository.delete(project.get());
    }

    @PostMapping("/{projectId}/roles")
    @EditPermission
    public ResponseEntity<List<ProjectRole>> addUsersToExistingProjectPost(
            @PathVariable Long projectId,
            @RequestBody List<String> usernames) {
        Project project = projectRepository.findById(projectId).get();
        return new ResponseEntity<>(projectService.addProjectRolesByUsernames(project, usernames), HttpStatus.OK);
    }

    @GetMapping(value = "/{projectId}", params = "roles")
    @EditPermission
    public ResponseEntity<ProjectData> getProjectWithProjectRoles(
            @PathVariable Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        return new ResponseEntity<>(projectService.getProjectData(optionalProject.get()), HttpStatus.OK);
    }

    @PostMapping("/{projectId}/leave")
    @AnyPermission
    public void leaveProject(@PathVariable Long projectId) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        projectRoleService.leaveProject(optionalProject.get());
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<Project> patchProject(
            @PathVariable("projectId") Long projectId,
            @RequestBody Project patchedProject
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            return new ResponseEntity<>(projectService.patchProject(optionalProject.get(), patchedProject), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProjectData(
            @PathVariable Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            return new ResponseEntity<>(optionalProject.get(), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{projectId}/projectRole")
    @AnyPermission
    public ResponseEntity<ProjectRoleData> getProjectRoleForCurrentUserByProjectId(@PathVariable Long projectId) {
        ProjectRoleData projectRoleData = projectService.getProjectRoleForCurrentUserByProjectId(projectId);
        if (projectRoleData != null) {
            return new ResponseEntity<>(projectRoleData, HttpStatus.OK);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
