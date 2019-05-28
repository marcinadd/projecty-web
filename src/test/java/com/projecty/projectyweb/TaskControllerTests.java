package com.projecty.projectyweb;

import com.projecty.projectyweb.model.*;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.repository.TaskRepository;
import com.projecty.projectyweb.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class TaskControllerTests {

    @MockBean
    UserRepository userRepository;
    @MockBean
    ProjectRepository projectRepository;
    @MockBean
    TaskRepository taskRepository;
    @MockBean
    RoleRepository roleRepository;

    @Autowired
    private MockMvc mockMvc;
    private Project project;

    @Before
    public void setup() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        User user1 = new User();
        user1.setId(2L);
        user1.setUsername("user1");

        project = new Project();
        project.setName("Test");
        project.setId(1L);

        List<Role> roles = new ArrayList<>();

        Role role = new Role();
        role.setId(1L);
        role.setUser(user);
        role.setProject(project);
        role.setName(Roles.ADMIN.toString());
        roles.add(role);

        Role role1 = new Role();
        role1.setId(2L);
        role1.setUser(user1);
        role1.setProject(project);
        role1.setName(Roles.USER.toString());
        roles.add(role1);


        List<Role> rolesUser = new ArrayList<>();
        rolesUser.add(role);
        user.setRoles(rolesUser);

        List<Role> rolesUser1 = new ArrayList<>();
        rolesUser1.add(role1);
        user1.setRoles(rolesUser1);

        project.setRoles(roles);


        List<Task> tasks = new ArrayList<>();
        Task task = new Task();
        task.setId(1L);
        task.setName("Test task");
        tasks.add(task);

        project.setTasks(tasks);

        Mockito.when(projectRepository.save(project))
                .thenReturn(project);
        Mockito.when(projectRepository.findById(1L))
                .thenReturn(Optional.ofNullable(project));
        Mockito.when(roleRepository.findRoleByUserAndProject(user, project))
                .thenReturn(role);
        Mockito.when(roleRepository.findRoleByUserAndProject(user1, project))
                .thenReturn(role1);
        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(user);
        Mockito.when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(user1);
    }

    @Test
    @WithMockUser
    public void givenRequestOnMyProject_shouldReturnMyprojectsViewWithTask() throws Exception {
        mockMvc.perform(get("/project/tasklist?projectId=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tasklist"))
                .andExpect(model().attribute("project", hasProperty("name", Matchers.equalTo("Test"))))
                .andExpect(model().attribute("project", hasProperty("tasks", hasItem(Matchers.<Task>hasProperty("name", Matchers.equalTo("Test task"))))));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteTask_shouldRedirectToTaskListView() throws Exception {
        mockMvc.perform(post("/project/deleteTask?projectId=1&taskId=1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/project/tasklist"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteTaskWhichNotExists_shouldReturn404() throws Exception {
        mockMvc.perform(post("/project/deleteTask?projectId=1&taskId=2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user1")
    public void givenRequestOnDeleteTaskOnUserWithoutPermissions_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/project/deleteTask?projectId=1&taskId=1"))
                .andExpect(status().isForbidden());
    }


}
