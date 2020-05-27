package com.projecty.projectyweb.project.role;

import com.projecty.projectyweb.project.Project;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRoleDataDTO {
    private Long id;
    private Project project;
    private ProjectRoles name;

    public ProjectRoleDataDTO(ProjectRole projectRole) {
        this.id = projectRole.getId();
        this.project = projectRole.getProject();
        this.name = projectRole.getName();
    }
}
