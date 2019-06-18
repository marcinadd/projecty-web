package com.projecty.projectyweb.service.role;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;

import java.util.List;

public interface RoleService {
    void save(Role role);

    boolean isValidRoleName(String roleName);

    void addRolesToProjectByUsernames(Project project, List<String> usernames);

    void addCurrentUserToProjectAsAdmin(Project project);
}
