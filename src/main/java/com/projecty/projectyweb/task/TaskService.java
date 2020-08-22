package com.projecty.projectyweb.task;


import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectService;
import com.projecty.projectyweb.project.role.ProjectRoleService;
import com.projecty.projectyweb.task.dto.ProjectTasksData;
import com.projecty.projectyweb.task.dto.TaskData;
import com.projecty.projectyweb.team.role.TeamRoleService;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRoleService projectRoleService;
    private final TeamRoleService teamRoleService;
    private final UserService userService;
    private final ProjectService projectService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ProjectRoleService projectRoleService, TeamRoleService teamRoleService, UserService userService, ProjectService projectService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRoleService = projectRoleService;
        this.teamRoleService = teamRoleService;
        this.userService = userService;
        this.projectService = projectService;
    }

    public void changeTaskStatus(Task task, String status) {
        task.setStatus(TaskStatus.valueOf(status));
        taskRepository.save(task);
    }

    public long getDayCountToStart(Long taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Date now = new Date();
            Date start = optionalTask.get().getStartDate();
            return TimeUnit.DAYS.convert(start.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
        }
        return Long.MIN_VALUE;
    }

    public long getDayCountToEnd(Long taskId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent()) {
            Date now = new Date();
            Date endDate = optionalTask.get().getEndDate();
            return TimeUnit.DAYS.convert(endDate.getTime() - now.getTime(), TimeUnit.MILLISECONDS);
        }
        return Long.MIN_VALUE;
    }

    public void updateTaskDetails(Task existingTask, Task newTask) {
        existingTask.setName(newTask.getName());
        existingTask.setStartDate(newTask.getStartDate());
        existingTask.setEndDate(newTask.getEndDate());
        existingTask.setStatus(newTask.getStatus());
        taskRepository.save(existingTask);
    }

    public User assignUserByUsername(Task task, String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent() && hasUserAccessToTask(task, optionalUser.get())) {
            User user = optionalUser.get();
            if (task.getAssignedUsers() == null) {
                List<User> assignedUsers = new ArrayList<>();
                assignedUsers.add(user);
                task.setAssignedUsers(assignedUsers);
                taskRepository.save(task);
            } else if (!task.getAssignedUsers().contains(user)) {
                task.getAssignedUsers().add(user);
                taskRepository.save(task);
            }
            return user;
        }
        return null;
    }

    public List<String> getNotAssignedUsernameListForTask(Task task) {
        Project project = task.getProject();
        if (project.getTeam() != null) {
            Set<User> teamRoleUsers = teamRoleService.getTeamRoleUsers(project.getTeam());
            teamRoleUsers.removeAll(task.getAssignedUsers());
            return userService.getUsernamesFromUserList(new ArrayList<>(teamRoleUsers));
        }
        Set<User> projectRoleUsers = projectRoleService.getProjectRoleUsersAndInvitedUsers(project);
        projectRoleUsers.removeAll(task.getAssignedUsers());
        return userService.getUsernamesFromUserList(new ArrayList<>(projectRoleUsers));
    }

    private boolean hasUserAccessToTask(Task task, User user) {
        return (task.getProject().getRoles() != null &&
                projectRoleService.getProjectRoleUsersAndInvitedUsers(task.getProject()).contains(user))
                || (task.getProject().getTeam() != null && (teamRoleService.getTeamRoleUsers(task.getProject().getTeam()).contains(user)));
    }

    public void removeAssignmentByUsername(Task task, String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            task.getAssignedUsers().remove(optionalUser.get());
            taskRepository.save(task);
        }
    }

    public boolean hasCurrentUserPermissionToEditOrIsAssignedToTask(Task task) {
        User user = userService.getCurrentUser();
        Project project = task.getProject();
        return projectService.hasCurrentUserPermissionToEdit(project) || task.getAssignedUsers().contains(user);
    }

    public Task findTaskInRepositoryAndUpdateFields(Task newTask) {
        if (newTask.getId() != null) {
            Optional<Task> optionalTask = taskRepository.findById(newTask.getId());
            if (optionalTask.isPresent()) {
                Task task = optionalTask.get();
                if (newTask.getName() != null) task.setName(newTask.getName());
                if (newTask.getStartDate() != null) task.setStartDate(newTask.getStartDate());
                if (newTask.getEndDate() != null) task.setEndDate(newTask.getEndDate());
                if (newTask.getStatus() != null) task.setStatus(newTask.getStatus());
                if (newTask.getImportance() != null) task.setImportance(newTask.getImportance());
                return task;
            }
        }
        return null;
    }

    public ProjectTasksData getProjectTasksData(Project project) {
        List<Task> toDoTasks = taskRepository.findByProjectAndStatusOrderByStartDate(project, TaskStatus.TO_DO);
        List<Task> inProgressTasks = taskRepository.findByProjectAndStatusOrderByEndDate(project, TaskStatus.IN_PROGRESS);
        List<Task> doneTasks = taskRepository.findByProjectAndStatus(project, TaskStatus.DONE);
        boolean hasPermissionToEdit = projectService.hasCurrentUserPermissionToEdit(project);
        return ProjectTasksData.builder()
                .toDoTasks(toDoTasks)
                .inProgressTasks(inProgressTasks)
                .doneTasks(doneTasks)
                .hasPermissionToEdit(hasPermissionToEdit)
                .project(project)
                .build();
    }

    @Transactional
    public void deleteTask(Task task) {
        Project project = task.getProject();
        project.getTasks().remove(task);
        projectService.save(project);
        taskRepository.delete(task);
    }

    public TaskData getTaskData(Task task) {
        TaskData taskData = new TaskData();
        taskData.setTask(task);
        taskData.setProjectId(task.getProject().getId());
        taskData.setNotAssignedUsernames(getNotAssignedUsernameListForTask(task));
        return taskData;
    }

    public Task addTaskToProject(Task task, Project project) {
        task.setStatus(TaskStatus.TO_DO);
        task.setProject(project);
        return taskRepository.save(task);
    }

    public List<Task> getUndoneAssignedTasksForCurrentUser() {
        User user = userService.getCurrentUser();
        return taskRepository.findByAssignedUsersContainsAndStatusIsNot(user, TaskStatus.DONE);
    }
}

