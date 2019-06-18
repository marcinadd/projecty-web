package com.projecty.projectyweb.service.project;

import com.projecty.projectyweb.model.Project;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


public interface ProjectService {
    void save(Project project);

    String checkCurrentUserAccessLevel(Project project);

    boolean isCurrentUserProjectAdmin(Project project);

    boolean isCurrentUserProjectUser(Project project);

    void createNewProjectAndSave(Project project, List<String> usernames, RedirectAttributes redirectAttributes);

    void deleteUserFromProject(Long projectId, Long userId);
}
