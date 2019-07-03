package com.projecty.projectyweb.role;

import com.projecty.projectyweb.configurations.AppConfig;
import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.misc.RedirectMessageTypes;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final MessageSource messageSource;

    public RoleService(RoleRepository roleRepository, UserService userService, ProjectRepository projectRepository, MessageSource messageSource) {
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.messageSource = messageSource;
    }

    public void save(Role role) {
        roleRepository.save(role);
    }

    public boolean isValidRoleName(String roleName) {
        return AppConfig.ROLE_NAMES.contains(roleName);
    }

    public void addRolesToProjectByUsernames(Project project, List<String> usernames, List<RedirectMessage> messages) {
        List<Role> roles = new ArrayList<>();
        if (usernames != null) {
            Set<User> users = userService.getUserSetByUsernamesWithoutCurrentUser(usernames);
            removeExistingUsersInProjectFromSet(users, project);
            for (User user : users
            ) {
                    Role role = new Role();
                    role.setProject(project);
                role.setUser(user);
                    role.setName(Roles.USER.toString());
                    roles.add(role);
                    RedirectMessage message = new RedirectMessage();
                    message.setType(RedirectMessageTypes.SUCCESS);
                    String text = messageSource.getMessage(
                            "role.add.success",
                            new Object[]{user.getUsername(), project.getName()},
                            Locale.getDefault());
                    message.setText(text);
                    messages.add(message);
            }
        }
        if (project.getRoles() == null) {
            project.setRoles(roles);
        } else if (roles.size() > 0) {
            project.getRoles().addAll(roles);
        }
    }

    private void removeExistingUsersInProjectFromSet(Set<User> users, Project project) {
        if (project.getId() != null) {
            Set<User> existingUsers = getProjectRoleUsers(project);
            users.removeAll(existingUsers);
        }
    }

    private Set<User> getProjectRoleUsers(Project project) {
        List<Role> projectRoles = roleRepository.findByProject(project);
        Set<User> users = new HashSet<>();
        for (Role projectRole : projectRoles
        ) {
            users.add(projectRole.getUser());
        }
        return users;
    }

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

    public void deleteUserFromProject(User user, Project project) {
        Optional<Role> toDeleteRole = roleRepository.findRoleByUserAndProject(user, project);
        if (toDeleteRole.isPresent()) {
            List<Role> roles = project.getRoles();
            roles.remove(toDeleteRole.get());
            project.setRoles(roles);
            projectRepository.save(project);
        }
    }
}
