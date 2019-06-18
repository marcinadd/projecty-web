package com.projecty.projectyweb.service.project;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.Roles;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.service.role.RoleService;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final RoleService roleService;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserService userService, RoleRepository roleRepository, UserRepository userRepository, MessageSource messageSource, RoleService roleService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
    }

    @Override
    public void save(Project project) {
        projectRepository.save(project);
    }

    @Override
    public String checkCurrentUserAccessLevel(Project project) {
        Role currentUserRole = roleRepository.findRoleByUserAndProject(userService.getCurrentUser(), project);
        if (currentUserRole != null) {
            return currentUserRole.getName();
        }
        return null;
    }

    @Override
    public boolean isCurrentUserProjectAdmin(Project project) {
        return checkCurrentUserAccessLevel(project).equals(Roles.ADMIN.toString());
    }

    @Override
    public boolean isCurrentUserProjectUser(Project project) {
        String accessLevel = checkCurrentUserAccessLevel(project);
        return accessLevel.equals(Roles.ADMIN.toString()) || accessLevel.equals(Roles.USER.toString());
    }

    @Override
    public void createNewProjectAndSave(Project project, List<String> usernames, RedirectAttributes redirectAttributes) {
        List<String> messagesSuccess = new ArrayList<>();
        List<String> messagesFailed = new ArrayList<>();
        roleService.addCurrentUserToProjectAsAdmin(project);
        roleService.addRolesToProjectByUsernames(project, usernames);
        projectRepository.save(project);
    }

    @Override
    public void deleteUserFromProject(Long projectId, Long userId) {

    }

}
