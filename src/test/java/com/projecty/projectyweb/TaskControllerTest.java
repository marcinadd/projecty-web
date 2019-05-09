package com.projecty.projectyweb;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Task;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.ProjectRepository;
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
public class TaskControllerTest {

    @MockBean
    UserRepository userRepository;
    @MockBean
    ProjectRepository projectRepository;
    @MockBean
    TaskRepository taskRepository;
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

        List<User> users = new ArrayList<>();
        users.add(user);

        project.setTasks(tasks);
        project.setUsers(users);

        List<Project> projects = new ArrayList<>();
        projects.add(project);
        user.setProjects(projects);

        Mockito.when(projectRepository.save(project))
                .thenReturn(project);


        Mockito.when(projectRepository.findById(1L))
                .thenReturn(Optional.ofNullable(project));

    }


    @Test
    @WithMockUser(username = "user")
    public void givenRequestOnMyProject_shouldReturnMyprojectsViewWithTask() throws Exception {
        mockMvc.perform(get("/project/1/tasklist"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tasklist"))
                .andExpect(model().attribute("project", hasProperty("name", Matchers.equalTo("Test"))))
                .andExpect(model().attribute("project", hasProperty("tasks", hasItem(Matchers.<Task>hasProperty("name", Matchers.equalTo("Test task"))))));
    }
    

}
