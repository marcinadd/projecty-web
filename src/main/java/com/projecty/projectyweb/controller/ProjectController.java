package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.Roles;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.service.project.ProjectService;
import com.projecty.projectyweb.service.user.UserService;
import com.projecty.projectyweb.validator.ProjectValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("project")
public class ProjectController {
    @Autowired
    ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectValidator projectValidator;

    @GetMapping("addproject")
    public ModelAndView addProject() {
        return new ModelAndView("fragments/addproject", "project", new Project());
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
                    User user = userRepository.findByUsername(username);
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
            projectRepository.save(project);
        }
        return "redirect:/project/myprojects";
    }

    @PostMapping("deleteproject")
    public String deleteProject(@RequestParam Long projectId) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
            projectRepository.delete(project.get());
        }
        return "redirect:/project/myprojects/";
    }

    @GetMapping("myprojects")
    public ModelAndView myProjects() {
        return new ModelAndView(
                "fragments/myprojects",
                "roles",
                userService.getCurrentUser().getRoles()
        );
    }

    @GetMapping("manageusers")
    public ModelAndView manageUsers(
            @RequestParam Long projectId
    ) {
        ModelAndView modelAndView = new ModelAndView("fragments/manageusers");
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent() && projectService.isCurrentUserProjectAdmin(project.get())) {
            modelAndView.addObject("project", project.get());
            modelAndView.addObject("currentUser", userService.getCurrentUser());
            return modelAndView;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("manageusers")
    public String addUserToExistingProject(
            @RequestParam Long projectId,
            @RequestParam(required = false) List<String> usernames,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isPresent() && projectService.isCurrentUserProjectAdmin(optionalProject.get())) {
            Project project = optionalProject.get();
            List<Role> toAddRoles = new ArrayList<>();

            for (String username : usernames
            ) {
                User toAddUser = userRepository.findByUsername(username);
                if (toAddUser != null && roleRepository.findRoleByUserAndProject(toAddUser, project) == null) {
                    Role role = new Role();
                    role.setUser(toAddUser);
                    role.setProject(project);
                    role.setName(Roles.USER.toString());
                    toAddRoles.add(role);
                }
            }
            project.getRoles().addAll(toAddRoles);
            projectRepository.save(project);
        }
        redirectAttributes.addAttribute("projectId", projectId);
        return "redirect:/project/manageusers";
    }

    @PostMapping("deleteuser")
    public String deleteUser(
            @RequestParam Long projectId,
            @RequestParam Long userId,
            RedirectAttributes redirectAttributes
    ) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        User current = userService.getCurrentUser();
        Optional<User> toDeleteOptionalUser = userRepository.findById(userId);
        if (toDeleteOptionalUser.isPresent() && !toDeleteOptionalUser.get().equals(current)
                && optionalProject.isPresent() && projectService.isCurrentUserProjectAdmin(optionalProject.get())
        ) {
            Role toDeleteRole = roleRepository.findRoleByUserAndProject(toDeleteOptionalUser.get(), optionalProject.get());
            Project project = optionalProject.get();
            List<Role> roles = project.getRoles();
            roles.remove(toDeleteRole);
            project.setRoles(roles);
            projectRepository.save(project);
        } else if (toDeleteOptionalUser.isPresent() && toDeleteOptionalUser.get().equals(current)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        redirectAttributes.addAttribute("projectId", projectId);
        return "redirect:/project/manageusers";
    }

    // TODO Fix possible conflict here while using on multiple users
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
