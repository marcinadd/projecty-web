package com.projecty.projectyweb.repository;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findRoleByUserAndProject(User user, Project project);
}
