package com.projecty.projectyweb.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.projecty.projectyweb.configurations.AnyPermission;
import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.misc.RedirectMessage;

@CrossOrigin()
@RestController
@RequestMapping("projects")
public class ProjectController {

	private final ProjectService projectService;

	private final ProjectValidator projectValidator;

	public ProjectController(ProjectService projectService, ProjectValidator projectValidator) {
		this.projectService = projectService;
		this.projectValidator = projectValidator;
	}

	@GetMapping("")
	public Map<String, Object> myProjects() {
		return projectService.getProjects();
	}

	@PostMapping("")
	public void addProjectPost(@Valid @RequestBody Project project, BindingResult bindingResult) throws BindException {
		projectValidator.validate(project, bindingResult);
		if (bindingResult.hasErrors()) {
			System.out.println(bindingResult.getAllErrors());
			throw new BindException(bindingResult);
		}
		System.out.println(project.getUsernames());
		List<RedirectMessage> redirectMessages = new ArrayList<>();
		projectService.createNewProjectAndSave(project, project.getUsernames(), redirectMessages);
	}

	@DeleteMapping("/{projectId}")
	@EditPermission
	public void deleteProject(@PathVariable Long projectId) {
		projectService.deleteProject(projectId);
	}

	@PostMapping("/{projectId}/roles")
	@EditPermission
	public void addUsersToExistingProjectPost(@PathVariable Long projectId,
			@RequestBody(required = false) String usernames) throws IOException {
		InputUsernameList data = new ObjectMapper().readValue(usernames, InputUsernameList.class);
		projectService.addUserToProject(projectId, data.getUsernames());
	}

	@GetMapping("/{projectId}")
	@EditPermission
	public Map<String, Object> manageProject(@PathVariable Long projectId) {
		return projectService.manageProject(projectId);
	}

	@PostMapping("/{projectId}/leave")
	@AnyPermission
	public void leaveProject(@PathVariable Long projectId) {
		projectService.leaveProject(projectId);
	}

	@PatchMapping("/{projectId}")
	public void changeNamePost(@PathVariable("projectId") Long projectId, @RequestBody Map<String, String> fields) {
		final String name = fields.get("name");
		projectService.rename(projectId, name);
	}
}
