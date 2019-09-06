package com.projecty.projectyweb.project.role;

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
public class ProjectRoleService {
    private final ProjectRoleRepository projectRoleRepository;
    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final MessageSource messageSource;

    public ProjectRoleService(ProjectRoleRepository projectRoleRepository, UserService userService, ProjectRepository projectRepository, MessageSource messageSource) {
        this.projectRoleRepository = projectRoleRepository;
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.messageSource = messageSource;
    }

    public void save(ProjectRole projectRole) {
        projectRoleRepository.save(projectRole);
    }

    public void addRolesToProjectByUsernames(Project project, List<String> usernames, List<RedirectMessage> messages) {
        List<ProjectRole> projectRoles = new ArrayList<>();
        if (usernames != null) {
            Set<User> users = userService.getUserSetByUsernamesWithoutCurrentUser(usernames);
            removeExistingUsersInProjectFromSet(users, project);
            for (User user : users
            ) {
                ProjectRole projectRole = new ProjectRole(ProjectRoles.USER, user, project);
                projectRoles.add(projectRole);
                    RedirectMessage message = new RedirectMessage();
                    message.setType(RedirectMessageTypes.SUCCESS);
                    String text = messageSource.getMessage(
                            "projectRole.add.success",
                            new Object[]{user.getUsername(), project.getName()},
                            Locale.getDefault());
                    message.setText(text);
                    messages.add(message);
            }
        }
        if (project.getProjectRoles() == null) {
            project.setProjectRoles(projectRoles);
        } else if (projectRoles.size() > 0) {
            project.getProjectRoles().addAll(projectRoles);
        }
    }

    private void removeExistingUsersInProjectFromSet(Set<User> users, Project project) {
        if (project.getId() != null) {
            Set<User> existingUsers = getProjectRoleUsers(project);
            users.removeAll(existingUsers);
        }
    }

    public Set<User> getProjectRoleUsers(Project project) {
        List<ProjectRole> projectRoles = projectRoleRepository.findByProject(project);
        Set<User> users = new HashSet<>();
        projectRoles.forEach(projectRole -> users.add(projectRole.getUser()));
        return users;
    }

    public void addCurrentUserToProjectAsAdmin(Project project) {
        User current = userService.getCurrentUser();
        ProjectRole projectRole = new ProjectRole(ProjectRoles.ADMIN, current, project);
        if (project.getProjectRoles() == null) {
            List<ProjectRole> projectRoles = new ArrayList<>();
            projectRoles.add(projectRole);
            project.setProjectRoles(projectRoles);
        } else {
            project.getProjectRoles().add(projectRole);
        }
    }

    public void deleteUserFromProject(User user, Project project) {
        Optional<ProjectRole> toDeleteRole = projectRoleRepository.findRoleByUserAndProject(user, project);
        if (toDeleteRole.isPresent()) {
            List<ProjectRole> projectRoles = project.getProjectRoles();
            projectRoles.remove(toDeleteRole.get());
            project.setProjectRoles(projectRoles);
            projectRepository.save(project);
        }
    }

    public void leaveProject(Project project, User user) throws NoAdminsInProjectException {
        Optional<ProjectRole> optionalProjectRole = projectRoleRepository.findRoleByUserAndProject(user, project);
        if (optionalProjectRole.isPresent()) {
            ProjectRole projectRole = optionalProjectRole.get();
            int admins = projectRoleRepository.countByProjectAndName(project, ProjectRoles.ADMIN);
            if ((projectRole.getName().equals(ProjectRoles.ADMIN) && admins - 1 > 0) || project.getName().equals(ProjectRoles.USER)) {
                project.getProjectRoles().remove(optionalProjectRole.get());
                projectRepository.save(project);
            } else {
                throw new NoAdminsInProjectException();
            }
        }
    }
}
