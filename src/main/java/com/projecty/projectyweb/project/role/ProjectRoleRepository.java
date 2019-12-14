package com.projecty.projectyweb.project.role;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRoleRepository extends JpaRepository<ProjectRole, Long> {
    Optional<ProjectRole> findRoleByUserAndProject(User user, Project project);

    List<ProjectRole> findByProjectOrderByIdAsc(Project project);
    int countByProjectAndName(Project project, ProjectRoles name);
}
