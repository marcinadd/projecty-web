package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.Roles;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.service.project.ProjectService;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
    private RoleRepository roleRepository;


    @GetMapping("addproject")
    public String addProject() {
        return "fragments/addproject";
    }

    @PostMapping("addproject")
    public RedirectView addProjectProcess(@ModelAttribute Project project,
                                          @RequestParam(required = false) List<String> usernames, BindingResult bindingResult) {

        User currentUser = userService.getCurrentUser();
        List<Role> roles = new ArrayList<>();
        if (usernames != null) {

            for (String username : usernames
            ) {
                User user = userService.findByUsername(username);
                if (user != null) {
                    Role role = new Role();
                    role.setProject(project);
                    role.setUser(user);
                    role.setName(Roles.USER.toString());
                    roles.add(role);
                }
            }
        }

        Role admin = new Role();
        admin.setProject(project);
        admin.setUser(currentUser);
        admin.setName(Roles.ADMIN.toString());
        roles.add(admin);
        project.setRoles(roles);
        System.out.println(project);
        projectService.save(project);
        RedirectView redirectView = new RedirectView("/project/myprojects");
        redirectView.setContextRelative(true);
        return redirectView;
    }

    @GetMapping("myprojects")
    public String myProjects(Model model) {
        User current = userService.getCurrentUser();
        model.addAttribute("roles", current.getRoles());
        System.out.println(current.getRoles());
        return "fragments/myprojects";
    }

    @GetMapping("manageusers")
    public String manageUsers(
            @RequestParam Long projectId,
            Model model) {

        Optional<Project> project = projectService.findById(projectId);

        if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
            model.addAttribute("project", project.get());
            model.addAttribute("currentUser", userService.getCurrentUser());
            return "fragments/manageusers";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("manageusers")
    public RedirectView addUserToExistingProject(
            @RequestParam Long projectId,
            @RequestParam(required = false) List<String> usernames
    ) {
        Optional<Project> optionalProject = projectService.findById(projectId);
        if (optionalProject.isPresent() && projectService.isCurrentUserProjectAdmin(optionalProject.get())) {
            Project project = optionalProject.get();
            List<Role> toAddRoles = new ArrayList<>();

            for (String username : usernames
            ) {
                User toAddUser = userService.findByUsername(username);
                if (toAddUser != null) {
                    Role role = new Role();
                    role.setUser(toAddUser);
                    role.setProject(project);
                    role.setName(Roles.USER.toString());
                    toAddRoles.add(role);
                }
            }
            project.getRoles().addAll(toAddRoles);
            projectService.save(project);
        }

        RedirectView redirectView = new RedirectView("manageusers?projectId=" + projectId);
        redirectView.setContextRelative(true);
        return redirectView;
    }

    @GetMapping("deleteuser")
    public RedirectView deleteUser(
            @RequestParam Long projectId,
            @RequestParam Long userId) {
        Optional<Project> optionalProject = projectService.findById(projectId);
        User current = userService.getCurrentUser();
        Optional<User> toDeleteOptionalUser = userService.findById(userId);
        if (toDeleteOptionalUser.isPresent() && !toDeleteOptionalUser.get().equals(current)
                && optionalProject.isPresent() && projectService.isCurrentUserProjectAdmin(optionalProject.get())) {

            Role toDeleteRole = roleRepository.findRoleByUserAndProject(toDeleteOptionalUser.get(), optionalProject.get());
            Project project = optionalProject.get();
            List<Role> roles = project.getRoles();
            roles.remove(toDeleteRole);
            project.setRoles(roles);
            projectService.save(project);
        } else if (toDeleteOptionalUser.isPresent() && toDeleteOptionalUser.get().equals(current)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        RedirectView redirectView = new RedirectView("manageusers?projectId=" + projectId);
        redirectView.setContextRelative(true);
        return redirectView;
    }
}
