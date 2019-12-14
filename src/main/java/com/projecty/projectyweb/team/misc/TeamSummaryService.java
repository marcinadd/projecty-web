package com.projecty.projectyweb.team.misc;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.team.Team;

import java.util.List;

public class TeamSummaryService {
    public static void generateTeamSummary(Team team) {
        TeamSummary summary = new TeamSummary();
        summary.setUserCount(team.getTeamRoles().size());
        summary.setProjectCount(team.getProjects().size());
        summary.setTaskCount(countTasks(team.getProjects()));
        team.setTeamSummary(summary);
    }

    private static int countTasks(List<Project> projects) {
        return projects.stream().mapToInt(project ->
                {
                    if (project.getTasks() != null) {
                        return project.getTasks().size();
                    }
                    return 0;
                }
        ).sum();
    }
}
