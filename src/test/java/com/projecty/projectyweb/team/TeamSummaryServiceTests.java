package com.projecty.projectyweb.team;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.task.Task;
import com.projecty.projectyweb.team.misc.TeamSummary;
import com.projecty.projectyweb.team.misc.TeamSummaryService;
import com.projecty.projectyweb.team.role.TeamRole;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class TeamSummaryServiceTests {
    @Test
    public void whenGenerateTeamSummary_shouldReturnTeamWithTeamSummary() {
        Team team = new Team();
        List<TeamRole> roles = new ArrayList<>();
        roles.add(new TeamRole());

        List<Project> projects = new ArrayList<>();
        Project project1 = new Project();
        project1.setTasks(Collections.singletonList(new Task()));
        Project project2 = new Project();
        List<Task> tasks = new ArrayList<>();
        tasks.add(new Task());
        tasks.add(new Task());
        project2.setTasks(tasks);

        projects.add(project1);
        projects.add(project2);

        team.setProjects(projects);
        team.setTeamRoles(roles);
        TeamSummaryService.generateTeamSummary(team);
        TeamSummary summary = team.getTeamSummary();
        assertThat(summary.getUserCount(), is(1));
        assertThat(summary.getProjectCount(), is(2));
        assertThat(summary.getTaskCount(), is(3));
    }
}
