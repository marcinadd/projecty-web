package com.projecty.projectyweb.project.role;

import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@CrossOrigin()
@RestController
@RequestMapping("projectRoles")
public class ProjectRoleController {
    private final UserService userService;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectRoleService projectRoleService;

    public ProjectRoleController(UserService userService, ProjectRoleRepository projectRoleRepository, ProjectRoleService projectRoleService) {
        this.userService = userService;
        this.projectRoleRepository = projectRoleRepository;
        this.projectRoleService = projectRoleService;
    }

    @DeleteMapping("/{roleId}")
    @EditPermission
    public void deleteUserPost(
            @PathVariable Long roleId
    ) {
        User current = userService.getCurrentUser();
        Optional<ProjectRole> toDeleteOptionalRole = projectRoleRepository.findById(roleId);
        if (toDeleteOptionalRole.isPresent() && !toDeleteOptionalRole.get().getUser().equals(current)) {
            projectRoleService.deleteRoleFromProject(toDeleteOptionalRole.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping("/{roleId}")
    @EditPermission
    public ResponseEntity<ProjectRole> changeRolePatch(
            @PathVariable Long roleId,
            @RequestBody ProjectRole patchedProjectRole
    ) {
        Optional<ProjectRole> optionalRole = projectRoleRepository.findById(roleId);
        User current = userService.getCurrentUser();
        if (optionalRole.isPresent() && !optionalRole.get().getUser().equals(current)) {
            return new ResponseEntity<>(projectRoleService.patchProjectRole(optionalRole.get(), patchedProjectRole), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
