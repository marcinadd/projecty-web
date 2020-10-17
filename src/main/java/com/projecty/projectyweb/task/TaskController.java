package com.projecty.projectyweb.task;

import com.projecty.projectyweb.configurations.EditPermission;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.project.ProjectService;
import com.projecty.projectyweb.task.dto.ProjectTasksData;
import com.projecty.projectyweb.task.dto.TaskData;
import com.projecty.projectyweb.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@CrossOrigin()
@RestController
@RequestMapping("tasks")
public class TaskController {
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final TaskValidator taskValidator;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public TaskController(ProjectRepository projectRepository, ProjectService projectService, TaskValidator taskValidator, TaskRepository taskRepository, TaskService taskService) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.taskValidator = taskValidator;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<Task> addTaskPost(
            @PathVariable Long projectId,
            @RequestBody Task task,
            BindingResult bindingResult
    ) {
        task.setId(null);
        taskValidator.validate(task, bindingResult);
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalProject.get())) {
            return new ResponseEntity<>(taskService.addTaskToProject(task, optionalProject.get()), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ProjectTasksData> getProjectTaskData(
            @PathVariable Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Project project = optionalProject.get();
        return new ResponseEntity<>(taskService.getProjectTasksData(project), HttpStatus.OK);
    }

    @DeleteMapping("/{taskId}")
    @EditPermission
    public void deleteTask(
            @PathVariable Long taskId
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        taskService.deleteTask(optionalTask.get());
    }

    @GetMapping("/{taskId}")
    @EditPermission
    public ResponseEntity<TaskData> getTask(
            @PathVariable Long taskId
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        return new ResponseEntity<>(taskService.getTaskData(optionalTask.get()), HttpStatus.OK);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<Task> editTaskDetailsPatch(
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
            return new ResponseEntity<>(taskRepository.save(newTaskCandidate), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{taskId}/assign")
    @EditPermission
    public ResponseEntity<User> assignUserPost(
            @PathVariable Long taskId,
            @RequestBody String username
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        Task task = optionalTask.get();
        return new ResponseEntity<>(taskService.assignUserByUsername(task, username), HttpStatus.OK);
    }

    @DeleteMapping("/{taskId}/assign/{username}")
    @EditPermission
    public void removeAssignment(
            @PathVariable Long taskId,
            @PathVariable String username
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        taskService.removeAssignmentByUsername(optionalTask.get(), username);
    }

    @GetMapping("assigned")
    public ResponseEntity<List<Task>> getUndoneAssignedTasksForCurrentUser() {
        return new ResponseEntity<>(taskService.getUndoneAssignedTasksForCurrentUser(), HttpStatus.OK);
    }
}
