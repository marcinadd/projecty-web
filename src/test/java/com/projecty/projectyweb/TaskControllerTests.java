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
        user.setUsername("user");
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(user);

        project = new Project();
        project.setName("Test");

        List<Task> tasks = new ArrayList<>();
        Task task = new Task();
        task.setName("Test task");
        tasks.add(task);

        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setUser(user);
        role.setProject(project);
        role.setName(Roles.ADMIN.toString());
        roles.add(role);

        project.setTasks(tasks);
        project.setRoles(roles);

        user.setRoles(roles);

        Mockito.when(projectRepository.save(project))
                .thenReturn(project);
        Mockito.when(projectRepository.findById(1L))
                .thenReturn(Optional.ofNullable(project));
        Mockito.when(roleRepository.findRoleByUserAndProject(user, project))
                .thenReturn(role);
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


}
