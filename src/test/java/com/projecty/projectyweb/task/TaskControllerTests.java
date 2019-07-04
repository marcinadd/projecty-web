package com.projecty.projectyweb.task;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
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
import org.springframework.test.context.ActiveProfiles;
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

@ActiveProfiles("test")
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
    ProjectRoleRepository projectRoleRepository;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setup() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        User user1 = new User();
        user1.setId(2L);
        user1.setUsername("user1");

        Project project = new Project();
        project.setName("Test");
        project.setId(1L);

        List<ProjectRole> projectRoles = new ArrayList<>();

        ProjectRole projectRole = new ProjectRole();
        projectRole.setId(1L);
        projectRole.setUser(user);
        projectRole.setProject(project);
        projectRole.setName(ProjectRoles.ADMIN);
        projectRoles.add(projectRole);

        ProjectRole projectRole1 = new ProjectRole();
        projectRole1.setId(2L);
        projectRole1.setUser(user1);
        projectRole1.setProject(project);
        projectRole1.setName(ProjectRoles.USER);
        projectRoles.add(projectRole1);


        List<ProjectRole> rolesUser = new ArrayList<>();
        rolesUser.add(projectRole);
        user.setProjectRoles(rolesUser);

        List<ProjectRole> rolesUser1 = new ArrayList<>();
        rolesUser1.add(projectRole1);
        user1.setProjectRoles(rolesUser1);

        project.setProjectRoles(projectRoles);


        List<Task> tasks = new ArrayList<>();
        Task task = new Task();
        task.setId(1L);
        task.setName("Test task");
        tasks.add(task);

        project.setTasks(tasks);

        Mockito.when(projectRepository.save(project))
                .thenReturn(project);
        Mockito.when(projectRepository.findById(1L))
                .thenReturn(Optional.of(project));
        Mockito.when(projectRoleRepository.findRoleByUserAndProject(user, project))
                .thenReturn(Optional.of(projectRole));
        Mockito.when(projectRoleRepository.findRoleByUserAndProject(user1, project))
                .thenReturn(Optional.of(projectRole1));
        Mockito.when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));
    }

    @Test
    @WithMockUser
    public void givenRequestOnMyProject_shouldReturnMyprojectsViewWithTask() throws Exception {
        mockMvc.perform(get("/project/task/tasklist?projectId=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/task/task-list"))
                .andExpect(model().attribute("project", hasProperty("name", Matchers.equalTo("Test"))))
                .andExpect(model().attribute("project", hasProperty("tasks", hasItem(Matchers.<Task>hasProperty("name", Matchers.equalTo("Test task"))))));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteTask_shouldRedirectToTaskListView() throws Exception {
        mockMvc.perform(post("/project/task/deleteTask?projectId=1&taskId=1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/project/task/tasklist"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnAddTask_shouldReturnAddTaskView() throws Exception {
        mockMvc.perform(get("/project/task/addtasks?projectId=1"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/task/add-task"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void givenRequestOnDeleteTaskOnUserWithoutPermissions_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/project/task/deleteTask?projectId=1&taskId=1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangeStatus_shouldRedirectToTaskListView() throws Exception {
        mockMvc.perform(post("/project/task/changeStatus?projectId=1&taskId=1&done=true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/project/task/tasklist"));
    }

    @Test
    @WithMockUser(username = "user1")
    public void givenRequestOnChangeStatusWhichUserWithoutPermissions_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/project/task/changeStatus?projectId=1&taskId=1&done=true"))
                .andExpect(status().isForbidden());
    }


}
