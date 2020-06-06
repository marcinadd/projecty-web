package com.projecty.projectyweb.team.role.dto;

import com.projecty.projectyweb.project.Project;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class TeamProjectsData {
    private String teamName;
    private List<Project> projects;
    private Boolean isCurrentUserTeamManager;
}
