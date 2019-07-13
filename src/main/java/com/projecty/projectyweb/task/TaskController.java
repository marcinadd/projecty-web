package com.projecty.projectyweb.task;

import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.project.ProjectService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_SUCCESS;

@Controller
@RequestMapping("project/task")
public class TaskController {
    private final ProjectRepository projectRepository;

    private final ProjectService projectService;

    private final TaskValidator taskValidator;

    private final TaskRepository taskRepository;

    private final TaskService taskService;

    private final MessageSource messageSource;

    public TaskController(ProjectRepository projectRepository, ProjectService projectService, TaskValidator taskValidator, TaskRepository taskRepository, TaskService taskService, MessageSource messageSource) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.taskValidator = taskValidator;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.messageSource = messageSource;
    }

    @GetMapping("addtasks")
    public ModelAndView addTasks(
            @RequestParam Long projectId
    ) {
        ModelAndView modelAndView = new ModelAndView("fragments/task/add-task");
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent() && projectService.hasCurrentUserPermissionToEdit(project.get())) {
            modelAndView.addObject("project", project.get());
            modelAndView.addObject("task", new Task());
            return modelAndView;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("addtasks")
    public ModelAndView addTasksPost(
            @RequestParam Long projectId,
            @ModelAttribute Task task,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        taskValidator.validate(task, bindingResult);
        redirectAttributes.addAttribute("projectId", projectId);
        Optional<Project> project = projectRepository.findById(projectId);
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("fragments/task/add-task");
            project.ifPresent(modelAndView::addObject);
            return modelAndView;
        } else if (project.isPresent() && projectService.hasCurrentUserPermissionToEdit(project.get())) {
            task.setStatus(TaskStatus.TO_DO);
            task.setProject(project.get());
            List<Task> tasks = project.get().getTasks();
            tasks.add(task);
            taskRepository.save(task);
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("task.add.success", new Object[]{task.getName(), project.get().getName()}, Locale.getDefault())));
            return new ModelAndView("redirect:taskList");
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("taskList")
    public ModelAndView taskList(
            @RequestParam Long projectId
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent() && projectService.hasCurrentUserPermissionToView(optionalProject.get())) {
            Project project = optionalProject.get();
            List<Task> toDoTasks = taskRepository.findByProjectAndStatusOrderByStartDate(project, TaskStatus.TO_DO);
            List<Task> inProgressTasks = taskRepository.findByProjectAndStatusOrderByEndDate(project, TaskStatus.IN_PROGRESS);
            List<Task> doneTasks = taskRepository.findByProjectAndStatus(project, TaskStatus.DONE);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("fragments/task/task-list");
            modelAndView.addObject("toDoTasks", toDoTasks);
            modelAndView.addObject("inProgressTasks", inProgressTasks);
            modelAndView.addObject("doneTasks", doneTasks);
            modelAndView.addObject("project", optionalProject.get());
            return modelAndView;
        }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("deleteTask")
    public ModelAndView deleteTaskPost(
            @RequestParam Long taskId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalTask.get().getProject())) {
            Task task = optionalTask.get();
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(
                    messageSource.getMessage("task.delete.success", new Object[]{task.getName()}, Locale.getDefault())));
            redirectAttributes.addAttribute("projectId", task.getProject().getId());
            taskRepository.delete(task);
            return new ModelAndView("redirect:taskList");
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("changeStatus")
    public ModelAndView changeStatusPost(
            @RequestParam Long taskId,
            @RequestParam String status,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalTask.get().getProject())) {
            Task task = optionalTask.get();
            taskService.changeTaskStatus(optionalTask.get(), status);
            redirectAttributes.addAttribute("projectId", task.getProject().getId());
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("task.status.change.success", new Object[]{optionalTask.get().getName()}, Locale.getDefault())));
            return new ModelAndView("redirect:taskList");
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("manageTask")
    public ModelAndView manageTask(
            @RequestParam Long taskId
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalTask.get().getProject())) {
            Task task = optionalTask.get();
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("fragments/task/manage-task");
            modelAndView.addObject("task", task);
            modelAndView.addObject("notAssignedUsernames", taskService.getNotAssignedUsernameListForTask(task));
            return modelAndView;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("editTaskDetails")
    public String editTaskDetailsPost(
            @ModelAttribute Task task,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        taskValidator.validate(task, bindingResult);
        if (bindingResult.hasErrors()) {
            return "fragments/task/manage-task";
        } else {
            Optional<Task> optionalTask = taskRepository.findById(task.getId());
            if (optionalTask.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalTask.get().getProject())) {
                taskService.updateTaskDetails(optionalTask.get(), task);
                redirectAttributes.addAttribute("projectId", optionalTask.get().getProject().getId());
                return "redirect:taskList";
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("assignUser")
    public String assignUserPost(
            Long taskId,
            String username,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalTask.get().getProject())) {
            Task task = optionalTask.get();
            taskService.assignUserByUsername(task, username);
            redirectAttributes.addAttribute("taskId", task.getId());
            return "redirect:manageTask";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("removeAssignment")
    public String removeAssignment(
            Long taskId,
            String username,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (optionalTask.isPresent() && projectService.hasCurrentUserPermissionToEdit(optionalTask.get().getProject())) {
            Task task = optionalTask.get();
            taskService.removeAssignmentByUsername(task, username);
            redirectAttributes.addAttribute("taskId", task.getId());
            return "redirect:manageTask";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
