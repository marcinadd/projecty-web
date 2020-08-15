package com.projecty.projectyweb.project.role;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
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
            Set<User> existingUsers = getProjectRoleUsers(project);
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
                    invitedRoles.add(new ProjectRole(ProjectRoles.USER, user, project, true));
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

    public Set<User> getProjectRoleUsers(Project project) {
        List<ProjectRole> projectRoles = projectRoleRepository.findByProjectOrderByIdAsc(project);
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

    public void deleteRoleFromProject(ProjectRole role) {
        Project project = role.getProject();
        List<ProjectRole> projectRoles = project.getProjectRoles();
        projectRoles.remove(role);
        project.setProjectRoles(projectRoles);
        projectRepository.save(project);
    }

    public void leaveProject(Project project) throws NoAdminsInProjectException {
        User user = userService.getCurrentUser();
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
        List<ProjectRole> projectRoles = project.getProjectRoles();
        List<ProjectRole> projectRoleInvitations = project.getProjectRoleInvitations();
        projectRoles.add(projectRole);
        projectRoleInvitations.remove(projectRole);
        project.setProjectRoles(projectRoles);
        project.setProjectRoleInvitations(projectRoleInvitations);
        projectRepository.save(project);
        return projectRole;
    }
}
