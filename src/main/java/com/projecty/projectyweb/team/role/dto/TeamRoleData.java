package com.projecty.projectyweb.team.role.dto;

import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoles;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamRoleData {
    private Long id;
    private Team team;
    private TeamRoles name;

    public TeamRoleData(TeamRole teamRole) {
        this.id = teamRole.getId();
        this.team = teamRole.getTeam();
        this.name = teamRole.getName();
    }
}
