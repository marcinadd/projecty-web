package com.projecty.projectyweb.role;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleByUserAndProject(User user, Project project);

    List<Role> findByProject(Project project);
}
