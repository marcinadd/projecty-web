package com.projecty.projectyweb.project.dto;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoles;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectsTeamData {
    private Long id;
    private Team team;
    private List<Project> projects;
    private TeamRoles role;

    public ProjectsTeamData(TeamRole teamRole) {
        this.id = teamRole.getId();
        this.team = teamRole.getTeam();
        this.projects = teamRole.getTeam().getProjects();
        this.role = teamRole.getName();
    }
}
