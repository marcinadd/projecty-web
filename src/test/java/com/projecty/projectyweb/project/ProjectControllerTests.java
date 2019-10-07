package com.projecty.projectyweb.project;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        Team team = new Team();
        TeamRole teamRole = new TeamRole();
        teamRole.setName(TeamRoles.MANAGER);
        teamRole.setTeam(team);
        teamRole.setUser(user);

        List<TeamRole> teamRoles = new ArrayList<>();
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
        mockMvc.perform(get("/project/myProjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("projectRoles").exists())
                .andExpect(jsonPath("teamRoles").exists());
    }

    @Test
    @WithMockUser
    public void givenRequestOnPostFormWithoutOtherUsers_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/project/addProject")
                .flashAttr("project", project))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnManageProject_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/project/manageProject?projectId=1")
                .flashAttr("project", project))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.project").isNotEmpty())
                .andExpect(jsonPath("$.projectRoles").isArray())
                .andExpect(jsonPath("$.currentUser.username").value(user.getUsername()));
    }

    @Test
    @WithMockUser
    public void givenRequestOnDeleteUser_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/project/deleteUser?projectId=1&userId=2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnAddUsers_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/project/addUsers?projectId=1")
                .param("usernames", "user1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangeRole_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/project/changeRole?projectId=1&roleId=2&newRoleName=USER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangeName_shouldReturnOk() throws Exception {
        Project editedProject = new Project();
        editedProject.setId(1L);
        editedProject.setName("New sample project");
        mockMvc.perform(post("/project/changeName")
                .flashAttr("project", editedProject))
                .andExpect(status().isOk());
    }
}
