package com.projecty.projectyweb.repository;

import com.projecty.projectyweb.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}
