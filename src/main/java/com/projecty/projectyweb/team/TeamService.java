package com.projecty.projectyweb.team;


import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.team.role.TeamRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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

    @Transactional
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
}
