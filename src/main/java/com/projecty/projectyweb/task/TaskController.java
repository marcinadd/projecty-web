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

    private final MessageSource messageSource;

    public TaskController(ProjectRepository projectRepository, ProjectService projectService, TaskValidator taskValidator, TaskRepository taskRepository, MessageSource messageSource) {
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.taskValidator = taskValidator;
        this.taskRepository = taskRepository;
        this.messageSource = messageSource;
    }

    @GetMapping("addtasks")
    public ModelAndView addTasks(
            @RequestParam Long projectId
    ) {
        ModelAndView modelAndView = new ModelAndView("fragments/task/add-task");
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
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
        } else if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
            task.setDone(false);
            List<Task> tasks = project.get().getTasks();
            tasks.add(task);
            taskRepository.save(task);
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("task.add.success", new Object[]{task.getName(), project.get().getName()}, Locale.getDefault())));
            return new ModelAndView("redirect:/project/task/tasklist");
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("tasklist")
    public ModelAndView taskList(
            @RequestParam Long projectId
    ) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent() && projectService.isCurrentUserProjectUser(project.get())) {
            return new ModelAndView("fragments/task/task-list", "project", project.get());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("deleteTask")
    public ModelAndView deleteTaskPost(
            @RequestParam Long projectId,
            @RequestParam Long taskId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> project = projectRepository.findById(projectId);
        Optional<Task> task = taskRepository.findById(taskId);
        if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get()) && task.isPresent()) {
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("task.delete.success", new Object[]{task.get().getName()}, Locale.getDefault())));
            taskRepository.delete(task.get());
            redirectAttributes.addAttribute("projectId", projectId);
            return new ModelAndView("redirect:/project/task/tasklist");
        } else if (project.isPresent() && task.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("changeStatus")
    public ModelAndView changeStatusPost(
            @RequestParam Long projectId,
            @RequestParam Long taskId,
            @RequestParam Boolean done,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> project = projectRepository.findById(projectId);
        Optional<Task> task = taskRepository.findById(taskId);
        if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get()) && task.isPresent()) {
            task.get().setDone(done);
            taskRepository.save(task.get());
            redirectAttributes.addAttribute("projectId", projectId);
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("task.status.change.success", new Object[]{task.get().getName()}, Locale.getDefault())));
            return new ModelAndView("redirect:/project/task/tasklist");
        } else if (project.isPresent() && task.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

}
