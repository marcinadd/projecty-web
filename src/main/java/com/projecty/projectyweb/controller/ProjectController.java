package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.service.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//@Controller
//@RequestMapping("project")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @GetMapping("addproject")
    public String addProject(Model model){
        model.addAttribute("project", new Project());
        return "fragments/addproject";
    }

    @PostMapping
    public String addProjectProcess(@ModelAttribute Project project, BindingResult bindingResult, Model model){
        System.out.println("Aaa");
        System.out.println(project);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            System.out.println(username);

        }
        projectService.save(project);
        return "index";
    }



}
