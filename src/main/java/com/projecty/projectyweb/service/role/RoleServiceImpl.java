package com.projecty.projectyweb.service.role;

import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.Roles;
import com.projecty.projectyweb.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void save(Role role) {
        roleRepository.save(role);
    }

    @Override
    public void changeRole(Role role, String newRoleName) {
        if (newRoleName.equals(Roles.ADMIN.toString())) {
            role.setName(Roles.ADMIN.toString());
        } else if (newRoleName.equals(Roles.USER.toString())) {
            role.setName(Roles.USER.toString());
        }
        roleRepository.save(role);
    }

}
