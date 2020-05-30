package com.projecty.projectyweb.project.dto;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProjectData {
    private Project project;
    private List<ProjectRole> projectRoles;
    private User currentUser;
}
