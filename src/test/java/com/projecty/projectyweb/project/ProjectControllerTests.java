package com.projecty.projectyweb.project;

import com.projecty.projectyweb.ProjectyWebApplication;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class ProjectControllerTests {

    @MockBean
    UserRepository userRepository;
    @MockBean
    ProjectRepository projectRepository;
    @MockBean
    ProjectRoleRepository projectRoleRepository;
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

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(Optional.of(user1));
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        Mockito.when(projectRepository.save(project))
                .thenReturn(project);
        Mockito.when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.ofNullable(project));
        Mockito.when(projectRoleRepository.findById(1L))
                .thenReturn(Optional.of(projectRole));
        Mockito.when(projectRoleRepository.findRoleByUserAndProject(user, project))
                .thenReturn(Optional.of(projectRole));
        Mockito.when(projectRoleRepository.findById(2L))
                .thenReturn(Optional.of(projectRole1));
        Mockito.when(projectRoleRepository.findRoleByUserAndProject(user1, project))
                .thenReturn(Optional.of(projectRole1));
    }

    @Test
    @WithMockUser
    public void givenRequestOnMyProject_shouldReturnMyprojectsView() throws Exception {
        mockMvc.perform(get("/project/myprojects"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("projectRoles"))
                .andExpect(view().name("fragments/project/my-projects"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnAddProject_shouldReturnAddprojectView() throws Exception {
        mockMvc.perform(get("/project/addproject"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/project/add-project"));
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
                .andExpect(view().name("fragments/project/manage-users"));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteCurrentUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/project/deleteuser?projectId=1&userId=1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteUser_shouldRedirectToManageUsers() throws Exception {
        mockMvc.perform(post("/project/deleteuser?projectId=1&userId=2"))
                .andExpect(redirectedUrl("/project/manageusers?projectId=1"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser
    public void givenRequestOnPostFormManageUsers_shouldRedirectToManageUsers() throws Exception {
        mockMvc.perform(post("/project/manageusers?projectId=1")
                .param("usernames", "user1"))
                .andExpect(redirectedUrl("/project/manageusers?projectId=1"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangeRole_shouldRedirect() throws Exception {
        mockMvc.perform(post("/project/changeRole?projectId=1&roleId=2&newRoleName=USER"))
                .andExpect(status().is3xxRedirection());
    }
}
