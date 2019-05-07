package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Task;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.service.project.ProjectService;
import com.projecty.projectyweb.service.task.TaskService;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @GetMapping("addproject")
    public String addProject() {
        return "fragments/addproject";
    }

    @PostMapping("addproject")
    public RedirectView addProjectProcess(@ModelAttribute Project project,
                                          @RequestParam(required = false) List<String> usernames, BindingResult bindingResult, Model model) {

        List<User> users = new ArrayList<>();
        if (usernames != null) {
            users.addAll(userService.findByUsernames(usernames));
        }

        users.add(userService.getCurrentUser());
        users.forEach(System.out::println);

        project.setUsers(users);
        System.out.println(project);
        projectService.save(project);

        RedirectView redirectView = new RedirectView("/project/myprojects");
        redirectView.setContextRelative(true);
        return redirectView;

    }

    @GetMapping("myprojects")
    public String myProjects(Model model) {
        User current = userService.getCurrentUser();
        model.addAttribute("projects", current.getProjects());
        return "fragments/myprojects";
    }

    @GetMapping("{projectId}/addtasks")
    public String addTasks(
            @PathVariable Long projectId, Model model
    ) {
        User current = userService.getCurrentUser();
        Optional<Project> project = projectService.findById(projectId);

        if (project.isPresent() && current.getProjects().contains(project.get())) {
            model.addAttribute("project", project.get());
            return "fragments/addtasks";
        }

        return "redirect:fragments/404";
    }

    @PostMapping("{projectId}/addtasks")
    public RedirectView addTasksPost(
            @PathVariable Long projectId,
            @ModelAttribute Task task,
            BindingResult bindingResult
    ) {
        User current = userService.getCurrentUser();
        Optional<Project> project = projectService.findById(projectId);

        if (project.isPresent() && current.getProjects().contains(project.get())) {
            List<Task> tasks = project.get().getTasks();
            tasks.add(task);
            taskService.save(task);
        }

        RedirectView redirectView = new RedirectView("/project/myprojects");
        redirectView.setContextRelative(true);
        return redirectView;

    }


}
