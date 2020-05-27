package com.projecty.projectyweb.project;

import com.projecty.projectyweb.project.role.ProjectRoleDataDTO;
import com.projecty.projectyweb.team.role.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ProjectsDataDTO {
    private List<ProjectRoleDataDTO> projectRoles;
    private List<TeamRole> teamRoles;
}
