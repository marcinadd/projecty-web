package com.projecty.projectyweb.team;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.user.UserService;


@Service
public class TeamService {
	
    private final TeamRepository teamRepository;
    private final TeamRoleService teamRoleService;
    private final UserService userService;
    private final ProjectRepository projectRepository;

    @Autowired
    public TeamService(TeamRepository teamRepository, TeamRoleService teamRoleService, ProjectRepository projectRepository, UserService userService) {
        this.userService = userService;
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

	public void delete(Long teamId) {
		Optional<Team> optionalTeam = findById(teamId);
		if(optionalTeam.isPresent()) {
			teamRepository.delete(optionalTeam.get());
		}
		else {
			// FIXME throw a 404 exception
		}
	}
	
	public Map<String, Object> findProjects(Long teamId) {
		Optional<Team> optionalTeam = findById(teamId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("teamName", optionalTeam.get().getName());
        map.put("projects", optionalTeam.get().getProjects());
        map.put("isCurrentUserTeamManager", teamRoleService.isCurrentUserTeamManager(optionalTeam.get()));
        return map;
	}

	public List<TeamRole> listMyTeam() {
		return userService.getCurrentUser().getTeamRoles();
	}

	public List<TeamRole> addProjectToTeam() {
		return teamRoleService.getTeamRolesWhereManager(userService.getCurrentUser());
	}

	public boolean addProjectToTeamPost(Long teamId, @Valid Project project) {
		Optional<Team> optionalTeam = findById(teamId);
		if (optionalTeam.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeam.get())) {
            createProjectForTeam(optionalTeam.get(), project);
            return true;
        }
		return false;
	}

	public Map<String, Object> manageTeam(Long teamId) {
		Optional<Team> optionalTeam = findById(teamId);
        Map<String, Object> map = new HashMap<>();
        map.put("team", optionalTeam.get());
        map.put("currentUser", userService.getCurrentUser());
        map.put("teamRoles", optionalTeam.get().getTeamRoles());
		return map;
	}

	public void savePost(Long teamId, List<String> usernames) {
		Optional<Team> optionalTeam = findById(teamId);
        teamRoleService.addTeamMembersByUsernames(optionalTeam.get(), usernames, null);
        save(optionalTeam.get());
	}

	public boolean deleteTeamRole(Long teamRoleId) {
		// TODO: 6/28/19 Prevent from delete current user from team
        Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
        	teamRoleService.delete(optionalTeamRole.get());
        	return true;
        }
		return false;
	}

	public boolean changeTeamRole(Long teamRoleId, String newRoleName) {
		Optional<TeamRole> optionalTeamRole = teamRoleService.findById(teamRoleId);
        if (optionalTeamRole.isPresent() && teamRoleService.isCurrentUserTeamManager(optionalTeamRole.get().getTeam())) {
            teamRoleService.changeTeamRole(optionalTeamRole.get(), newRoleName);
            return true;
        }
		return false;
	}

	public void leaveTeam(Long teamId) {
		Optional<Team> optionalTeam = findById(teamId);
		if(optionalTeam.isPresent()) {
			teamRoleService.leaveTeam(optionalTeam.get(), null);
		}
		
	}
}
