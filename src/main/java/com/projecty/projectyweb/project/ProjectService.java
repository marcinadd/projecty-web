package com.projecty.projectyweb.project;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoleService;
import com.projecty.projectyweb.project.role.ProjectRoles;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.team.role.TeamRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectRoleService projectRoleService;
    private final TeamRoleRepository teamRoleRepository;

    public ProjectService(ProjectRepository projectRepository, UserService userService, ProjectRoleRepository projectRoleRepository, ProjectRoleService projectRoleService, TeamRoleRepository teamRoleRepository) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.projectRoleRepository = projectRoleRepository;
        this.projectRoleService = projectRoleService;
        this.teamRoleRepository = teamRoleRepository;
    }

    public void save(Project project) {
        projectRepository.save(project);
    }

    public boolean hasCurrentUserPermissionToEdit(Project project) {
        User current = userService.getCurrentUser();
        if (project.getTeam() != null) {
            Optional<TeamRole> optionalTeamRole = teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current);
            return optionalTeamRole.isPresent() && optionalTeamRole.get().getName().equals(TeamRoles.MANAGER);
        }
        Optional<ProjectRole> optionalRole = projectRoleRepository.findRoleByUserAndProject(current, project);
        return optionalRole.isPresent() && optionalRole.get().getName().equals(ProjectRoles.ADMIN);
    }

    public boolean hasCurrentUserPermissionToView(Project project) {
        User current = userService.getCurrentUser();
        if (project.getTeam() != null) {
            return teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current).isPresent();
        }
        return hasUserRoleInProject(current, project);
    }

    public boolean hasUserRoleInProject(User user, Project project) {
        return projectRoleRepository.findRoleByUserAndProject(user, project).isPresent();
    }

    void createNewProjectAndSave(Project project, List<String> usernames, List<RedirectMessage> messages) {
        projectRoleService.addCurrentUserToProjectAsAdmin(project);
        projectRoleService.addRolesToProjectByUsernames(project, usernames, messages);
        projectRepository.save(project);
    }

    void changeName(Project existingProject, String newName) {
        existingProject.setName(newName);
        projectRepository.save(existingProject);
    }
}
