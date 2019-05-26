package com.projecty.projectyweb.service.project;

import com.projecty.projectyweb.model.Project;


public interface ProjectService {
    void save(Project project);

    String checkCurrentUserAccessLevel(Project project);

    boolean isCurrentUserProjectAdmin(Project project);

    boolean isCurrentUserProjectUser(Project project);
}
