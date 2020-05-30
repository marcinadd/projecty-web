package com.projecty.projectyweb.project.dto;

import com.projecty.projectyweb.project.role.ProjectRoleDataDTO;
import com.projecty.projectyweb.team.role.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProjectsData {
    private List<ProjectRoleDataDTO> projectRoles;
    private List<TeamRole> teamRoles;
}
