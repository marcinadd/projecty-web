package com.projecty.projectyweb.service.role;

import com.projecty.projectyweb.model.Role;

public interface RoleService {
    void save(Role role);

    void changeRole(Role role, String newRoleName);
}
