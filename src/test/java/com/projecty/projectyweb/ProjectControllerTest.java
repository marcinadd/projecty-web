package com.projecty.projectyweb;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.UserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @MockBean
    UserRepository userRepository;
    @MockBean
    ProjectRepository projectRepository;
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

        List<Project> projects = new ArrayList<>();
        projects.add(project);
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user1);

        project.setUsers(users);
        user.setProjects(projects);

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(user);
        Mockito.when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(user1);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(java.util.Optional.of(user));
        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(java.util.Optional.of(user1));
        Mockito.when(projectRepository.save(project))
                .thenReturn(project);
        Mockito.when(projectRepository.findById(project.getId()))
                .thenReturn(java.util.Optional.ofNullable(project));
    }

    @Test
    @WithMockUser
    public void givenRequestOnMyProject_shouldReturnMyprojectsView() throws Exception {
        mockMvc.perform(get("/project/myprojects"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/myprojects"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnAddProject_shouldReturnAddprojectView() throws Exception {
        mockMvc.perform(get("/project/addproject"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/addproject"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnPostFormWithoutOtherUsers_shouldRedirectToMyProjects() throws Exception {
        mockMvc.perform(post("/project/addproject")
                .flashAttr("project", project))
                .andExpect(redirectedUrl("/project/myprojects"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser
    public void givenRequestOnManageUsers_shouldReturnUserList() throws Exception {
        mockMvc.perform(get("/project/manageusers?projectId=1")
                .flashAttr("project", project))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/manageusers"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteCurrentUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/project/deleteuser?projectId=1&userId=1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteUser_shouldRedirectToManageUsers() throws Exception {
        mockMvc.perform(get("/project/deleteuser?projectId=1&userId=2"))
                .andExpect(redirectedUrl("manageusers?projectId=1"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser
    public void givenRequestOnPostFormManageUsers_shouldRedirectToManageUsers() throws Exception {
        mockMvc.perform(post("/project/manageusers?projectId=1")
                .param("usernames", "user1"))
                .andExpect(redirectedUrl("manageusers?projectId=1"))
                .andExpect(status().isFound());
    }

}
