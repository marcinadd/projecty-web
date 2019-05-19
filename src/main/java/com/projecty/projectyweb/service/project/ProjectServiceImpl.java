package com.projecty.projectyweb.service.project;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.Roles;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void save(Project project) {
        projectRepository.save(project);
    }

    @Override
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
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
        return checkCurrentUserAccessLevel(project).equals(Roles.USER.toString());
    }
}
