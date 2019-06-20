package com.projecty.projectyweb.service.role;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.User;

import java.util.List;

public interface RoleService {
    void save(Role role);

    boolean isValidRoleName(String roleName);

    void addRolesToProjectByUsernames(Project project, List<String> usernames, List<RedirectMessage> messages);

    void addCurrentUserToProjectAsAdmin(Project project);

    void deleteUserFromProject(User user, Project project);
}
