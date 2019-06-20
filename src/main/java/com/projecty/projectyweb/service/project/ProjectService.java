package com.projecty.projectyweb.service.project;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.model.Project;

import java.util.List;


public interface ProjectService {
    void save(Project project);

    String checkCurrentUserAccessLevel(Project project);

    boolean isCurrentUserProjectAdmin(Project project);

    boolean isCurrentUserProjectUser(Project project);

    void createNewProjectAndSave(Project project, List<String> usernames, List<RedirectMessage> messages);

}
