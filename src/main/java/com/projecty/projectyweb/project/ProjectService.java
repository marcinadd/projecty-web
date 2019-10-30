package com.projecty.projectyweb.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoleService;
import com.projecty.projectyweb.project.role.ProjectRoles;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.team.role.TeamRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final UserService userService;
	private final ProjectRoleRepository projectRoleRepository;
	private final ProjectRoleService projectRoleService;
	private final TeamRoleRepository teamRoleRepository;

	public ProjectService(ProjectRepository projectRepository, UserService userService,
			ProjectRoleRepository projectRoleRepository, ProjectRoleService projectRoleService,
			TeamRoleRepository teamRoleRepository) {
		this.projectRepository = projectRepository;
		this.userService = userService;
		this.projectRoleRepository = projectRoleRepository;
		this.projectRoleService = projectRoleService;
		this.teamRoleRepository = teamRoleRepository;
	}

	public void save(Project project) {
		projectRepository.save(project);
	}

	public boolean hasCurrentUserPermissionToEdit(Project project) {
		User current = userService.getCurrentUser();
		if (project.getTeam() != null) {
			Optional<TeamRole> optionalTeamRole = teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current);
			return optionalTeamRole.isPresent() && optionalTeamRole.get().getName().equals(TeamRoles.MANAGER);
		}
		Optional<ProjectRole> optionalRole = projectRoleRepository.findRoleByUserAndProject(current, project);
		return optionalRole.isPresent() && optionalRole.get().getName().equals(ProjectRoles.ADMIN);
	}

	public boolean hasCurrentUserPermissionToView(Project project) {
		User current = userService.getCurrentUser();
		if (project.getTeam() != null) {
			return teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current).isPresent();
		}
		return hasUserRoleInProject(current, project);
	}

	public boolean hasUserRoleInProject(User user, Project project) {
		return projectRoleRepository.findRoleByUserAndProject(user, project).isPresent();
	}

	public Map<String, Object> getProjects() {
		User current = userService.getCurrentUser();
		Map<String, Object> map = new HashMap<>();
		map.put("projectRoles", current.getProjectRoles());
		map.put("teamRoles", current.getTeamRoles());
		return map;
	}

	public Optional<Project> findById(Long projectId) {
		return projectRepository.findById(projectId);
	}

	public void deleteProject(Long projectId) {
		Optional<Project> project = findById(projectId);
		if (project.isPresent()) {
			projectRepository.delete(project.get());
		}
	}

	public void addUserToProject(Long projectId, List<String> usernameList) {
		Optional<Project> optionalProject = findById(projectId);
		Project project = optionalProject.get();
		List<RedirectMessage> redirectMessages = new ArrayList<>();
		projectRoleService.addRolesToProjectByUsernames(project, usernameList, redirectMessages);
		projectRepository.save(project);
	}

	public Map<String, Object> manageProject(Long projectId) {
		Optional<Project> optionalProject = projectRepository.findById(projectId);
		Map<String, Object> map = new HashMap<>();
		map.put("project", optionalProject.get());
		map.put("projectRoles", optionalProject.get().getProjectRoles());
		map.put("currentUser", userService.getCurrentUser());
		return map;
	}

	public void leaveProject(Long projectId) {
		Optional<Project> optionalProject = projectRepository.findById(projectId);
		User current = userService.getCurrentUser();
		projectRoleService.leaveProject(optionalProject.get(), current);
	}

	void createNewProjectAndSave(Project project, List<String> usernames, List<RedirectMessage> messages) {
		projectRoleService.addCurrentUserToProjectAsAdmin(project);
		projectRoleService.addRolesToProjectByUsernames(project, usernames, messages);
		projectRepository.save(project);
	}

	void changeName(Project existingProject, String newName) {
		
	}

	public void rename(Long projectId, String name) {
		Optional<Project> optionalProject = projectRepository.findById(projectId);
		if (optionalProject.isPresent() && hasCurrentUserPermissionToEdit(optionalProject.get())
				&& StringUtils.isEmpty(name)) {
			Project project = optionalProject.get();
			project.setName(name.trim());
			projectRepository.save(project);
		} else {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
	}
}
