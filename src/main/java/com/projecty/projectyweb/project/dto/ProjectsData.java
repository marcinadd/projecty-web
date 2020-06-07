package com.projecty.projectyweb.project.dto;

import com.projecty.projectyweb.project.role.ProjectRoleDataDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProjectsData {
    private List<ProjectRoleDataDTO> projectRoles;
    private List<ProjectsTeamData> teamProjects;
}
