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
        user.setUsername("user");
        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(user);

        project = new Project();
        project.setName("Test");
        Mockito.when(projectRepository.save(project))
                .thenReturn(project);


    }


    @Test
    @WithMockUser(username = "user")
    public void givenRequestOnMyProject_shouldReturnMyprojectsView() throws Exception {
        mockMvc.perform(get("/project/myprojects"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/myprojects"));
    }

    @Test
    @WithMockUser(username = "user")
    public void givenRequestOnAddProject_shouldReturnAddprojectView() throws Exception {
        mockMvc.perform(get("/project/addproject"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/addproject"));
    }

    @Test
    @WithMockUser(username = "user")
    public void givenRequestOnPostFormWithoutOtherUsers_shouldRedirectToMyProjects() throws Exception {
        mockMvc.perform(post("/project/addproject")
                .flashAttr("project", project))
                .andExpect(redirectedUrl("/project/myprojects"))
                .andExpect(status().isFound());
    }


}
