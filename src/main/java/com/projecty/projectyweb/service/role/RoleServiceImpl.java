package com.projecty.projectyweb.service.role;

import com.projecty.projectyweb.configurations.AppConfig;
import com.projecty.projectyweb.helpers.UserHelper;
import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.Roles;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserHelper userHelper;
    private final ProjectRepository projectRepository;

    public RoleServiceImpl(RoleRepository roleRepository, UserRepository userRepository, UserService userService, UserHelper userHelper, ProjectRepository projectRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userHelper = userHelper;
        this.projectRepository = projectRepository;
    }

    @Override
    public void save(Role role) {
        roleRepository.save(role);
    }

    @Override
    public boolean isValidRoleName(String roleName) {
        return AppConfig.ROLE_NAMES.contains(roleName);
    }

    @Override
    public void addRolesToProjectByUsernames(Project project, List<String> usernames) {
        List<Role> roles = new ArrayList<>();
        if (usernames != null) {
            usernames = userHelper.cleanUsernames(usernames);
            for (String username : usernames
            ) {
                User user = userRepository.findByUsername(username);
                if (user != null) {
                    Role role = new Role();
                    role.setProject(project);
                    role.setUser(user);
                    role.setName(Roles.USER.toString());
                    roles.add(role);
                }
            }
        }
        if (project.getRoles() == null) {
            project.setRoles(roles);
        } else if (roles.size() > 0) {
            project.getRoles().addAll(roles);
        }
    }

    @Override
    public void addCurrentUserToProjectAsAdmin(Project project) {
        User current = userService.getCurrentUser();
        Role role = new Role();
        role.setProject(project);
        role.setName(Roles.ADMIN.toString());
        role.setUser(current);
        if (project.getRoles() == null) {
            List<Role> roles = new ArrayList<>();
            roles.add(role);
            project.setRoles(roles);
        } else {
            project.getRoles().add(role);
        }
    }

    @Override
    public void deleteUserFromProject(User user, Project project) {
        Role toDeleteRole = roleRepository.findRoleByUserAndProject(user, project);
        List<Role> roles = project.getRoles();
        roles.remove(toDeleteRole);
        project.setRoles(roles);
        projectRepository.save(project);
    }
}
