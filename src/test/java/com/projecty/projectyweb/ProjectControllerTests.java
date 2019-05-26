package com.projecty.projectyweb;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.Role;
import com.projecty.projectyweb.model.Roles;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.ProjectRepository;
import com.projecty.projectyweb.repository.RoleRepository;
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
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class ProjectControllerTests {

    @MockBean
    UserRepository userRepository;
    @MockBean
    ProjectRepository projectRepository;
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

        Mockito.when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(user);
        Mockito.when(userRepository.findByUsername(user1.getUsername()))
                .thenReturn(user1);
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));
        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));
        Mockito.when(projectRepository.save(project))
                .thenReturn(project);
        Mockito.when(projectRepository.findById(project.getId()))
                .thenReturn(Optional.ofNullable(project));
        Mockito.when(roleRepository.findById(1L))
                .thenReturn(Optional.of(role));
        Mockito.when(roleRepository.findRoleByUserAndProject(user, project))
                .thenReturn(role);
        Mockito.when(roleRepository.findById(2L))
                .thenReturn(Optional.of(role1));
        Mockito.when(roleRepository.findRoleByUserAndProject(user1, project))
                .thenReturn(role1);
    }

    @Test
    @WithMockUser
    public void givenRequestOnMyProject_shouldReturnMyprojectsView() throws Exception {
        mockMvc.perform(get("/project/myprojects"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("roles"))
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
        mockMvc.perform(post("/project/deleteuser?projectId=1&userId=1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteUser_shouldRedirectToManageUsers() throws Exception {
        mockMvc.perform(post("/project/deleteuser?projectId=1&userId=2"))
                .andExpect(redirectedUrl("manageusers?projectId=1"))
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
        mockMvc.perform(post("/project/changerole?projectId=1&roleId=2"))
                .andExpect(status().is3xxRedirection());
    }
}
