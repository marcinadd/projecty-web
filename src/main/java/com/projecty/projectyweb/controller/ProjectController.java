package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.Roles;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.service.project.ProjectService;
import com.projecty.projectyweb.service.user.UserService;
import com.projecty.projectyweb.validator.ProjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;
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

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectValidator projectValidator;

    @GetMapping("addproject")
    public String addProject(Model model) {
        model.addAttribute("project", new Project());
        return "fragments/addproject";
    }

    @PostMapping("addproject")
    public String addProjectProcess(
            @Valid @ModelAttribute Project project,
            @RequestParam(required = false) List<String> usernames, BindingResult bindingResult
    ) {
        projectValidator.validate(project, bindingResult);
        if (bindingResult.hasErrors()) {
            return "fragments/addproject";
        } else {
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
            projectService.save(project);
        }
        return "redirect:/project/myprojects";
    }

    @PostMapping("deleteproject")
    public String deleteProject(@RequestParam Long projectId) {
        Optional<Project> project = projectService.findById(projectId);
        if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
            projectRepository.delete(project.get());
        }
        return "redirect:/project/myprojects/";
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
            Model model
    ) {
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
                if (toAddUser != null && roleRepository.findRoleByUserAndProject(toAddUser, project) == null) {
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

    @PostMapping("deleteuser")
    public RedirectView deleteUser(
            @RequestParam Long projectId,
            @RequestParam Long userId
    ) {
        Optional<Project> optionalProject = projectService.findById(projectId);
        User current = userService.getCurrentUser();
        Optional<User> toDeleteOptionalUser = userService.findById(userId);
        if (toDeleteOptionalUser.isPresent() && !toDeleteOptionalUser.get().equals(current)
                && optionalProject.isPresent() && projectService.isCurrentUserProjectAdmin(optionalProject.get())
        ) {
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

    @PostMapping("changerole")
    public String changeRole(
            @RequestParam Long projectId,
            @RequestParam Long roleId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalProject.isPresent() && projectService.isCurrentUserProjectAdmin(optionalProject.get()) && optionalRole.isPresent()) {
            Role role = optionalRole.get();
            if (role.getName().equals(Roles.ADMIN.toString())) {
                role.setName(Roles.USER.toString());
            } else {
                role.setName(Roles.ADMIN.toString());
            }
            roleRepository.save(role);
            redirectAttributes.addAttribute("projectId", projectId);
            redirectAttributes.addAttribute("roleId", roleId);
            return "redirect:/project/manageusers";
        } else if (optionalProject.isPresent() && optionalRole.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
