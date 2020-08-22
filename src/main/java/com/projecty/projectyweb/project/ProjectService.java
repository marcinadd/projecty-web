package com.projecty.projectyweb.project;

import com.projecty.projectyweb.group.GroupRepository;
import com.projecty.projectyweb.group.GroupService;
import com.projecty.projectyweb.project.dto.ProjectData;
import com.projecty.projectyweb.project.dto.ProjectsData;
import com.projecty.projectyweb.project.dto.ProjectsTeamData;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoleService;
import com.projecty.projectyweb.project.role.dto.ProjectRoleData;
import com.projecty.projectyweb.role.Roles;
import com.projecty.projectyweb.task.TaskRepository;
import com.projecty.projectyweb.task.TaskStatus;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.team.role.TeamRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProjectService extends GroupService<ProjectRole> {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectRoleService projectRoleService;
    private final TeamRoleRepository teamRoleRepository;
    private final TaskRepository taskRepository;

    public ProjectService(ProjectRepository projectRepository, UserService userService, ProjectRoleRepository projectRoleRepository, ProjectRoleService projectRoleService, TeamRoleRepository teamRoleRepository, TaskRepository taskRepository, GroupRepository groupRepository) {
        super(groupRepository);
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.projectRoleRepository = projectRoleRepository;
        this.projectRoleService = projectRoleService;
        this.teamRoleRepository = teamRoleRepository;
        this.taskRepository = taskRepository;
    }

    public void save(Project project) {
        projectRepository.save(project);
    }

    // -
    public boolean hasCurrentUserPermissionToEdit(Project project) {
        User current = userService.getCurrentUser();
        if (project.getTeam() != null) {
            Optional<TeamRole> optionalTeamRole = teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current);
            return optionalTeamRole.isPresent() && optionalTeamRole.get().getName().equals(TeamRoles.MANAGER);
        }
        Optional<ProjectRole> optionalRole = projectRoleRepository.findRoleByUserAndProject(current, project);
        return optionalRole.isPresent() && optionalRole.get().getName().equals(Roles.MANAGER);
    }

    public boolean hasCurrentUserPermissionToView(Project project) {
        User current = userService.getCurrentUser();
        if (project.getTeam() != null) {
            return teamRoleRepository.findByTeamAndAndUser(project.getTeam(), current).isPresent();
        }
        return hasUserRoleInProject(current, project);
    }

    // +
    public boolean hasUserRoleInProject(User user, Project project) {
        return projectRoleRepository.findRoleByUserAndProject(user, project).isPresent();
    }

    // +
    Project createNewProjectAndSave(Project project, List<String> usernames) {
        projectRoleService.addCurrentUserToProjectAsAdmin(project);
        projectRoleService.addRolesToProjectByUsernames(project, usernames);
        return projectRepository.save(project);
    }

    public List<ProjectRole> addProjectRolesByUsernames(Project project, List<String> usernames) {
        List<ProjectRole> unsavedProjectRoles = projectRoleService.addRolesToProjectByUsernames(project, usernames);
        return projectRoleService.saveProjectRoles(unsavedProjectRoles);
    }

    public ProjectsData getProjectsForCurrentUser() {
        User user = userService.getCurrentUser();
        List<ProjectRoleData> projectRoles = new ArrayList<>();
        user.getProjectRoles().forEach(projectRole -> projectRoles.add(new ProjectRoleData(projectRole)));

        List<ProjectsTeamData> teamRoles = new ArrayList<>();
        user.getTeamRoles().forEach(teamRole -> teamRoles.add(new ProjectsTeamData(teamRole)));
        return addSummaryToProjectsData(new ProjectsData(projectRoles, teamRoles));
    }

    public List<ProjectRoleData> getInvitationsForCurrentUser() {
        User user = userService.getCurrentUser();
        List<ProjectRoleData> projectRoleInvitations = new ArrayList<>();
        user.getProjectRoleInvitations().forEach(projectRole -> projectRoleInvitations.add(new ProjectRoleData(projectRole)));
        return projectRoleInvitations;
    }

    public ProjectData getProjectData(Project project) {
        List<ProjectRole> projectRoles = project.getRoles();
        projectRoles.sort(Comparator.comparing(ProjectRole::getId));
        return ProjectData.builder()
                .project(project)
                .projectRoles(projectRoles)
                .currentUser(userService.getCurrentUser())
                .build();
    }

    private ProjectsData addSummaryToProjectsData(ProjectsData projectsData) {
        projectsData.getProjectRoles().forEach(projectRoleData -> addSummaryToProject(projectRoleData.getProject()));
        projectsData.getTeamProjects().forEach(projectsTeamData -> projectsTeamData.getProjects().forEach(this::addSummaryToProject));
        return projectsData;
    }

    void addSummaryToProject(Project project) {
        Map<TaskStatus, Long> map = new LinkedHashMap<>();
        map.put(TaskStatus.TO_DO, taskRepository.countByProjectAndStatus(project, TaskStatus.TO_DO));
        map.put(TaskStatus.IN_PROGRESS, taskRepository.countByProjectAndStatus(project, TaskStatus.IN_PROGRESS));
        map.put(TaskStatus.DONE, taskRepository.countByProjectAndStatus(project, TaskStatus.DONE));
        project.setTaskSummary(map);
    }

    public ProjectRoleData getProjectRoleForCurrentUserByProjectId(Long projectId) {
        User currentUser = userService.getCurrentUser();
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent()) {
            Optional<ProjectRole> optionalProjectRole = projectRoleRepository.findRoleByUserAndProject(currentUser, optionalProject.get());
            return optionalProjectRole.map(ProjectRoleData::new).orElse(null);
        }
        return null;
    }
}
