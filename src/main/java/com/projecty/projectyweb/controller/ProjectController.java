package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.service.project.ProjectService;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @GetMapping("addproject")
    public String addProject() {
        return "fragments/addproject";
    }

    @PostMapping("addproject")
    public String addProjectProcess(@ModelAttribute Project project,
                                    @RequestParam List<String> usernames, BindingResult bindingResult, Model model) {

        List<User> users = userService.findByUsernames(usernames);
        users.add(userService.getCurrentUser());
        users.forEach(System.out::println);

        project.setUsers(users);
        System.out.println(project);
        projectService.save(project);
        return "index";

    }

    @GetMapping("myprojects")
    public String myProjects(Model model){
        User current=userService.getCurrentUser();
        model.addAttribute("projects", current.getProjects());
        current.getProjects().forEach(System.out::println);
        return "/fragments/myprojects";
    }


}
