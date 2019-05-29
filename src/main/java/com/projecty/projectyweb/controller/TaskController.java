package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Task;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.repository.TaskRepository;
import com.projecty.projectyweb.service.project.ProjectService;
import com.projecty.projectyweb.service.task.TaskService;
import com.projecty.projectyweb.service.user.UserService;
import com.projecty.projectyweb.validator.TaskValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("project/task")
public class TaskController {
    @Autowired
    UserService userService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectService projectService;

    @Autowired
    TaskService taskService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    TaskValidator taskValidator;

    @Autowired
    TaskRepository taskRepository;

    @GetMapping("addtasks")
    public ModelAndView addTasks(
            @RequestParam Long projectId
    ) {
        ModelAndView modelAndView = new ModelAndView("fragments/addtasks");
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
            modelAndView.setViewName("fragments/addtasks");
            project.ifPresent(modelAndView::addObject);
            return modelAndView;
        } else if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
            task.setDone(false);
            List<Task> tasks = project.get().getTasks();
            tasks.add(task);
            taskService.save(task);
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
            return new ModelAndView("fragments/tasklist", "project", project.get());
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
            return new ModelAndView("redirect:/project/task/tasklist");
        } else if (project.isPresent() && task.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

}
