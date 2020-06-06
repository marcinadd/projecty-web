package com.projecty.projectyweb.team;


import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.team.misc.TeamSummaryService;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.team.role.dto.TeamProjectsData;
import com.projecty.projectyweb.team.role.dto.TeamRoleData;
import com.projecty.projectyweb.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    public Project createProjectForTeam(Team team, Project project) {
        project.setTeam(team);
        project = projectRepository.save(project);
        team.getProjects().add(project);
        teamRepository.save(team);
        return project;
    }

    public TeamProjectsData getTeamProjects(Team team) {
        return TeamProjectsData.builder()
                .teamName(team.getName())
                .projects(team.getProjects())
                .isCurrentUserTeamManager(teamRoleService.isCurrentUserTeamManager(team))
                .build();
    }

    public Team editTeam(Team team, Team patchedTeam) {
        if (!patchedTeam.getName().isEmpty()) {
            team.setName(patchedTeam.getName());
        }
        return teamRepository.save(team);
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
