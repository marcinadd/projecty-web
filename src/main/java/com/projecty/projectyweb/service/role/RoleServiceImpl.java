package com.projecty.projectyweb.service.role;

import com.projecty.projectyweb.model.Role;
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

}
