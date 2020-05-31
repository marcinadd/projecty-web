package com.projecty.projectyweb.team;


import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.team.misc.TeamSummaryService;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.team.role.dto.TeamRoleData;
import com.projecty.projectyweb.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamRoleService teamRoleService;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    @Autowired
    public TeamService(TeamRepository teamRepository, TeamRoleService teamRoleService, ProjectRepository projectRepository, UserService userService) {
        this.teamRepository = teamRepository;
        this.teamRoleService = teamRoleService;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public Team createTeamAndSave(Team team, List<String> usernames) {
        teamRoleService.addCurrentUserAsTeamManager(team);
        teamRoleService.addTeamMembersByUsernames(team, usernames);
        return teamRepository.save(team);
    }

    public void createProjectForTeam(Team team, Project project) {
        project.setTeam(team);
        projectRepository.save(project);
        if (team.getProjects() == null) {
            List<Project> projects = new ArrayList<>();
            projects.add(project);
            team.setProjects(projects);
        } else {
            team.getProjects().add(project);
        }
        teamRepository.save(team);
    }

    public void editTeam(Team team, Map<String, String> fields) {
        String name = fields.get("name");
        if (!name.isEmpty()) {
            team.setName(name);
            teamRepository.save(team);
        }
    }

	public Optional<Team> findById(Long teamId) {
        return teamRepository.findById(teamId);
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }

    public void delete(Team team) {
        teamRepository.delete(team);
    }

    public List<TeamRoleData> getTeams() {
        List<TeamRole> teamRoles = userService.getCurrentUser().getTeamRoles();
        teamRoles.forEach(t -> TeamSummaryService.generateTeamSummary(t.getTeam()));
        List<TeamRoleData> teamRoleData = new ArrayList<>();
        teamRoles.forEach(t -> teamRoleData.add(new TeamRoleData(t)));
        return teamRoleData;
    }
}
