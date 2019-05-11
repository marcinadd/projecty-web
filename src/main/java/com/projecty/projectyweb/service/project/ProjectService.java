package com.projecty.projectyweb.service.project;

import com.projecty.projectyweb.model.Project;

import java.util.Optional;

public interface ProjectService {
    void save(Project project);

    Optional<Project> findById(Long id);

    boolean checkIfIsPresentAndContainsCurrentUser(Optional<Project> project);
}
