package com.projecty.projectyweb.task;

import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.project.ProjectService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@CrossOrigin()
@RestController
@RequestMapping("tasks")
public class TaskController {
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final TaskValidator taskValidator;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public TaskController(ProjectRepository projectRepository, ProjectService projectService, TaskValidator taskValidator, TaskRepository taskRepository, TaskService taskService, MessageSource messageSource) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.taskValidator = taskValidator;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @PostMapping("/project/{projectId}")
    public void addTaskPost(
            @PathVariable Long projectId,
            @ModelAttribute Task task,
            BindingResult bindingResult
    ) {
        taskValidator.validate(task, bindingResult);
        Optional<Project> project = projectRepository.findById(projectId);
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (project.isPresent() && projectService.hasCurrentUserPermissionToEdit(project.get())) {
            task.setStatus(TaskStatus.TO_DO);
            task.setProject(project.get());
            List<Task> tasks = project.get().getTasks();
            tasks.add(task);
            taskRepository.save(task);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/project/{projectId}")
    public Map<String, Object> taskList(
            @PathVariable Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Project project = optionalProject.get();
        List<Task> toDoTasks = taskRepository.findByProjectAndStatusOrderByStartDate(project, TaskStatus.TO_DO);
        List<Task> inProgressTasks = taskRepository.findByProjectAndStatusOrderByEndDate(project, TaskStatus.IN_PROGRESS);
        List<Task> doneTasks = taskRepository.findByProjectAndStatus(project, TaskStatus.DONE);
        Map<String, Object> map = new HashMap<>();
        map.put("toDoTasks", toDoTasks);
        map.put("inProgressTasks", inProgressTasks);
        map.put("doneTasks", doneTasks);
        map.put("project", optionalProject.get());
        map.put("hasPermissionToEdit", projectService.hasCurrentUserPermissionToEdit(project));
        return map;
    }

    @DeleteMapping("/{taskId}")
    @EditPermission
    public void deleteTaskPost(
            @PathVariable Long taskId
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        Task task = optionalTask.get();
        taskRepository.delete(task);
    }

    @GetMapping("/{taskId}")
    @EditPermission
    public Map<String, Object> getTask(
            @PathVariable Long taskId
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        Task task = optionalTask.get();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("task", task);
        map.put("projectId", task.getProject().getId());
        map.put("notAssignedUsernames", taskService.getNotAssignedUsernameListForTask(task));
        return map;
    }

    @PatchMapping("/{taskId}")
    public void editTaskDetailsPost(
            @PathVariable Long taskId,
            @RequestBody Task task
    ) throws BindException {
        task.setId(taskId);
        Task newTaskCandidate = taskService.findTaskInRepositoryAndUpdateFields(task);
        if (newTaskCandidate != null && projectService.hasCurrentUserPermissionToEdit(newTaskCandidate.getProject())) {
            DataBinder dataBinder = new DataBinder(newTaskCandidate);
            dataBinder.setValidator(taskValidator);
            dataBinder.validate();
            BindingResult result = dataBinder.getBindingResult();
            if (result.hasErrors()) {
                throw new BindException(result);
            }
            taskRepository.save(newTaskCandidate);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{taskId}/assign")
    @EditPermission
    public void assignUserPost(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> fields
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        Task task = optionalTask.get();
        taskService.assignUserByUsername(task, fields.get("username"));
    }

    @DeleteMapping("/{taskId}/assign")
    @EditPermission
    public void removeAssignment(
            @PathVariable Long taskId,
            @RequestBody Map<String, String> fields
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        Task task = optionalTask.get();
        taskService.removeAssignmentByUsername(task, fields.get("username"));
    }
}
