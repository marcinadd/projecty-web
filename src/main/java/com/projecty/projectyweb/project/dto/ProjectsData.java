package com.projecty.projectyweb.project.dto;

import com.projecty.projectyweb.project.role.dto.ProjectRoleData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProjectsData {
    private List<ProjectRoleData> projectRoles;
    private List<ProjectsTeamData> teamProjects;
}
