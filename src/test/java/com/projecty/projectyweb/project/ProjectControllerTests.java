package com.projecty.projectyweb.project;

import com.google.gson.Gson;
import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.project.role.ProjectRoles;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @MockBean
    UserService userService;
    @Autowired
    private MockMvc mockMvc;

    private Project project;
    private User user;
    @Before
    public void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("user");
        User user1 = new User();
        user1.setId(2L);
        user1.setUsername("user1");

        project = new Project();
        project.setName("Test");
        project.setId(1L);

        List<ProjectRole> projectRoles = new ArrayList<>();

        ProjectRole projectRole = new ProjectRole(ProjectRoles.ADMIN, user, project);
        projectRole.setId(1L);
        projectRoles.add(projectRole);

        ProjectRole projectRole1 = new ProjectRole(ProjectRoles.USER, user1, project);
        projectRole1.setId(2L);
        projectRoles.add(projectRole1);

        Team team = new Team();
        team.setProjects(new ArrayList<>());
        TeamRole teamRole = new TeamRole(TeamRoles.MANAGER, user, team);

        List<TeamRole> teamRoles = new ArrayList<>();
        teamRoles.add(teamRole);
        user.setTeamRoles(teamRoles);

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
        Mockito.when(projectRepository.save(any(Project.class)))
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
        Mockito.when(userService.getCurrentUser())
                .thenReturn(user);
    }

    @Test
    @WithMockUser
    public void givenRequestOnMyProject_shouldReturnProjectRolesAndTeamRoles() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("projectRoles").exists())
                .andExpect(jsonPath("teamProjects").exists());
    }

    @Test
    @WithMockUser
    public void givenRequestOnPostFormWithoutOtherUsers_shouldReturnOk() throws Exception {
        Project project1 = project;
        project1.setProjectRoles(null);
        mockMvc.perform(post("/projects").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(project1)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnManageProject_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/projects/1?roles=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project").isNotEmpty())
                .andExpect(jsonPath("$.projectRoles").isArray())
                .andExpect(jsonPath("$.currentUser.username").value(user.getUsername()));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteRole_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/projectRoles/2").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnAddUsers_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/projects/1/roles").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(Collections.singletonList("user2"))))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser
    public void givenRequestOnChangeName_shouldReturnOk() throws Exception {
        Project editedProject = new Project();
        editedProject.setId(1L);
        editedProject.setName("New sample project");
        mockMvc.perform(patch("/projects/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(editedProject)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnGetProject_shouldReturnProject() throws Exception {
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(project.getName()));
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangeRole_shouldReturnOk() throws Exception {
        mockMvc.perform(patch("/projectRoles/2").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + ProjectRoles.ADMIN + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnGetProjectRoleForCurrentUserByProjectId_shouldReturnProjectRoleData() throws Exception {
        mockMvc.perform(get("/projects/1/projectRole"))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.project.id").value(project.getId()))
                .andExpect(jsonPath("$.project.name").value(project.getName()));
    }
}
