package com.projecty.projectyweb.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private Project project;

    private Task task;

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
        task = new Task();
        task.setId(1L);
        task.setName("Test task");
        task.setProject(project);
        task.setAssignedUsers(new ArrayList<>());
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
    public void givenRequestOnMyProject_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/tasks/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toDoTasks").isArray())
                .andExpect(jsonPath("$.inProgressTasks").isArray())
                .andExpect(jsonPath("$.doneTasks").isArray())
                .andExpect(jsonPath("$.project.name").value(project.getName()))
                .andExpect(jsonPath("$.hasPermissionToEdit").value(true));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteTask_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/tasks/1").with(csrf()))
                .andExpect(status().isOk());
    }



    @Test
    @WithMockUser(username = "user1")
    public void givenRequestOnDeleteTaskOnUserWithoutPermissions_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/tasks/1").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void givenRequestOnManageTask_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.task.name").value(task.getName()))
                .andExpect(jsonPath("$.projectId").value(task.getProject().getId()))
                .andExpect(jsonPath("$.notAssignedUsernames").isArray());
    }

    @Test
    @WithMockUser
    public void givenRequestOnEditTaskDetails_shouldReturnOk() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setName("Sample name");
        task.setStartDate(new Date(System.currentTimeMillis()));
        task.setEndDate(new Date(System.currentTimeMillis()));
        task.setStatus(TaskStatus.TO_DO);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

        mockMvc.perform(patch("/tasks/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(gson.toJson(task)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnAssignUser_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/tasks/1/assign").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"user1\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnRemoveAssignment_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/tasks/1/assign/user1").with(csrf()))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser
    public void givenRequestOnGetUndoneAssignedTasks_shouldReturnUndoneAssignedTasks() throws Exception {
        mockMvc.perform(get("/tasks/assigned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
