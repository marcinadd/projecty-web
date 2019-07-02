package com.projecty.projectyweb.project;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.role.Role;
import com.projecty.projectyweb.role.RoleRepository;
import com.projecty.projectyweb.role.RoleService;
import com.projecty.projectyweb.role.Roles;
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
    private final RoleRepository roleRepository;
    private final RoleService roleService;
    private final TeamRoleRepository teamRoleRepository;

    public ProjectService(ProjectRepository projectRepository, UserService userService, RoleRepository roleRepository, RoleService roleService, TeamRoleRepository teamRoleRepository) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.roleService = roleService;
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
        Optional<Role> optionalRole = roleRepository.findRoleByUserAndProject(current, project);
        return optionalRole.isPresent() && optionalRole.get().getName().equals(Roles.ADMIN.toString());
    }

    public boolean hasCurrentUserPermissionToView(Project project) {
        User current = userService.getCurrentUser();
        if (project.getTeam() != null) {
            return teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current).isPresent();
        }
        return roleRepository.findRoleByUserAndProject(current, project).isPresent();
    }

    void createNewProjectAndSave(Project project, List<String> usernames, List<RedirectMessage> messages) {
        roleService.addCurrentUserToProjectAsAdmin(project);
        roleService.addRolesToProjectByUsernames(project, usernames, messages);
        projectRepository.save(project);
    }
}
