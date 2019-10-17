package com.projecty.projectyweb.team;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.team.role.TeamRoleService;


@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamRoleService teamRoleService;
    private final ProjectRepository projectRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, TeamRoleService teamRoleService, ProjectRepository projectRepository) {
        this.teamRepository = teamRepository;
        this.teamRoleService = teamRoleService;
        this.projectRepository = projectRepository;
    }

    public Team createTeamAndSave(Team team, List<String> usernames, List<RedirectMessage> redirectMessages) {
        teamRoleService.addCurrentUserAsTeamManager(team);
        teamRoleService.addTeamMembersByUsernames(team, usernames, redirectMessages);
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

    public void changeTeamName(Team team, String newName) {
        team.setName(newName);
        teamRepository.save(team);
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
}
