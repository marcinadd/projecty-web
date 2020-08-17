package com.projecty.projectyweb.project.role.dto;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.role.Roles;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRoleData {
    private Long id;
    private Project project;
    private Roles name;

    public ProjectRoleData(ProjectRole projectRole) {
        this.id = projectRole.getId();
        this.project = projectRole.getProject();
        this.name = projectRole.getName();
    }
}
