package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Task;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.service.project.ProjectService;
import com.projecty.projectyweb.service.task.TaskService;
import com.projecty.projectyweb.service.user.UserService;
import com.projecty.projectyweb.validator.TaskValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("project")
public class TaskController {

    @Autowired
    UserService userService;

    @Autowired
    ProjectService projectService;

    @Autowired
    TaskService taskService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    TaskValidator taskValidator;

    @GetMapping("addtasks")
    public String addTasks(
            @RequestParam Long projectId, Model model
    ) {
        Optional<Project> project = projectService.findById(projectId);
        if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
            model.addAttribute("project", project.get());
            model.addAttribute("task", new Task());
            return "fragments/addtasks";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("addtasks")
    public String addTasksPost(
            @RequestParam Long projectId,
            @ModelAttribute Task task,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        taskValidator.validate(task, bindingResult);
        redirectAttributes.addAttribute("projectId", projectId);
        Optional<Project> project = projectService.findById(projectId);
        if (bindingResult.hasErrors()) {
            project.ifPresent(model::addAttribute);
            return "fragments/addtasks";
        } else if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
            List<Task> tasks = project.get().getTasks();
            tasks.add(task);
            taskService.save(task);
            return "redirect:/project/tasklist";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("tasklist")
    public String taskList(
            @RequestParam Long projectId,
            Model model
    ) {
        Optional<Project> project = projectService.findById(projectId);
        if (project.isPresent() && projectService.isCurrentUserProjectUser(project.get())) {
            model.addAttribute("project", project.get());
            return "fragments/tasklist";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
