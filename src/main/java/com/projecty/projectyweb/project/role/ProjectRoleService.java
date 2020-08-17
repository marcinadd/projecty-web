package com.projecty.projectyweb.project.role;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.role.Roles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProjectRoleService {
    private final ProjectRoleRepository projectRoleRepository;
    private final UserService userService;
    private final ProjectRepository projectRepository;

    public ProjectRoleService(ProjectRoleRepository projectRoleRepository, UserService userService, ProjectRepository projectRepository) {
        this.projectRoleRepository = projectRoleRepository;
        this.userService = userService;
        this.projectRepository = projectRepository;
    }

    public void save(ProjectRole projectRole) {
        projectRoleRepository.save(projectRole);
    }

    private void removeExistingUsersInProjectFromSet(Set<User> users, Project project) {
        if (project.getId() != null) {
            Set<User> existingUsers = getProjectRoleUsersAndInvitedUsers(project);
            users.removeAll(existingUsers);
        }
    }

    public List<ProjectRole> addRolesToProjectByUsernames(Project project, List<String> usernames) {
        List<ProjectRole> invitedRoles = new ArrayList<>();
        if (usernames != null) {
            Set<User> users = userService.getUserSetByUsernamesWithoutCurrentUser(usernames);
            removeExistingUsersInProjectFromSet(users, project);
            users.forEach(user -> {
                if (user.getSettings().getCanBeAddedToProject()) {
                    invitedRoles.add(new ProjectRole(Roles.MEMBER, user, project, true));
                }
            });
        }
        if (project.getProjectRoleInvitations() == null) {
            project.setProjectRoleInvitations(invitedRoles);
        } else if (invitedRoles.size() > 0) {
            project.getProjectRoleInvitations().addAll(invitedRoles);
        }
        return invitedRoles;
    }

    public List<ProjectRole> saveProjectRoles(List<ProjectRole> projectRoles) {
        List<ProjectRole> savedProjectRoles = new ArrayList<>();
        projectRoles.forEach(projectRole -> savedProjectRoles.add(projectRoleRepository.save(projectRole)));
        return savedProjectRoles;
    }

    public Set<User> getProjectRoleUsersAndInvitedUsers(Project project) {
        List<ProjectRole> projectRoles = projectRoleRepository.findByProjectOrderByIdAsc(project);
        Set<User> users = new HashSet<>();
        projectRoles.forEach(projectRole -> {
            if (projectRole.getUser() != null) {
                users.add(projectRole.getUser());
            } else {
                users.add(projectRole.getInvitedUser());
            }
        });
        return users;
    }

    public void addCurrentUserToProjectAsAdmin(Project project) {
        User current = userService.getCurrentUser();
        ProjectRole projectRole = new ProjectRole(Roles.MANAGER, current, project);
        if (project.getRoles() == null) {
            List<ProjectRole> projectRoles = new ArrayList<>();
            projectRoles.add(projectRole);
            project.setRoles(projectRoles);
        } else {
            project.getRoles().add(projectRole);
        }
    }

    public void deleteRoleFromProject(ProjectRole role) {
        Project project = role.getProject();
        List<ProjectRole> projectRoles = project.getRoles();
        projectRoles.remove(role);
        project.setRoles(projectRoles);
        projectRepository.save(project);
    }

    public void deleteInvitationFromProject(ProjectRole projectRole) {
        deleteRoleFromProject(projectRole);
    }

    public void leaveProject(Project project) throws NoAdminsInProjectException {
        User user = userService.getCurrentUser();
        Optional<ProjectRole> optionalProjectRole = projectRoleRepository.findRoleByUserAndProject(user, project);
        if (optionalProjectRole.isPresent()) {
            ProjectRole projectRole = optionalProjectRole.get();
            int admins = projectRoleRepository.countByProjectAndName(project, Roles.MANAGER);
            if ((projectRole.getName().equals(Roles.MANAGER) && admins - 1 > 0)) {
                project.getRoles().remove(optionalProjectRole.get());
                projectRepository.save(project);
            } else {
                throw new NoAdminsInProjectException();
            }
        }
    }

    public ProjectRole patchProjectRole(ProjectRole projectRole, ProjectRole patchedData) {
        if (!projectRole.getName().equals(patchedData.getName())) {
            projectRole.setName(patchedData.getName());
            return projectRoleRepository.save(projectRole);
        }
        return projectRole;
    }

    public ProjectRole acceptInvitation(ProjectRole projectRole) {
        projectRole.setUser(projectRole.getInvitedUser());
        projectRole.setInvitedUser(null);
        projectRole = projectRoleRepository.save(projectRole);

        Project project = projectRole.getProject();
        List<ProjectRole> projectRoles = project.getRoles();
        List<ProjectRole> projectRoleInvitations = project.getProjectRoleInvitations();
        projectRoles.add(projectRole);
        projectRoleInvitations.remove(projectRole);
        project.setRoles(projectRoles);
        project.setProjectRoleInvitations(projectRoleInvitations);
        projectRepository.save(project);
        return projectRole;
    }
}
