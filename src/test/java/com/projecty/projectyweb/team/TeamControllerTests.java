package com.projecty.projectyweb.team;

import com.google.gson.Gson;
import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.message.MessageRepository;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.team.role.TeamRoleService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
@AutoConfigureMockMvc
public class TeamControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    TeamRoleRepository teamRoleRepository;

    @MockBean
    private TeamRepository teamRepository;

    @MockBean
    private UserService userService;
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ProjectRepository projectRepository;
    @Autowired
    private TeamRoleService teamRoleService;
    @MockBean
    private MessageRepository messageRepository;

    private Team team;
    private Project project;
    private Project newProject;

    @Before
    public void init() {
        team = new Team();
        team.setId(1L);
        team.setName("team");

        User user = new User();
        user.setUsername("user");
        user.setId(2L);

        User user1 = new User();
        user1.setId(3L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");

        TeamRole teamRole = new TeamRole();
        teamRole.setId(4L);
        teamRole.setUser(user);
        teamRole.setName(TeamRoles.MANAGER);
        teamRole.setTeam(team);

        TeamRole teamRole2 = new TeamRole();
        teamRole2.setId(5L);
        teamRole2.setUser(user1);
        teamRole2.setName(TeamRoles.MANAGER);
        teamRole2.setTeam(team);

        List<TeamRole> teamRoles = new ArrayList<>();
        teamRoles.add(teamRole);
        teamRoles.add(teamRole2);

        team.setTeamRoles(teamRoles);
        user.setTeamRoles(teamRoles);
        project = new Project();
        project.setName("Sample");
        List<Project> projects = new ArrayList<>();
        projects.add(project);
        team.setProjects(projects);

        newProject = new Project();
        newProject.setName("Sample new project");

        Mockito.when(teamRepository.findById(team.getId()))
                .thenReturn(java.util.Optional.ofNullable(team));

        Mockito.when(userService.getCurrentUser())
                .thenReturn(user);

        Mockito.when(userService.getUserSetByUsernamesWithoutCurrentUser(Collections.singletonList("user1")))
                .thenReturn(Collections.singleton(user1));

        Mockito.when(teamRepository.save(any(Team.class)))
                .thenReturn(team);

        Mockito.when(projectRepository.save(any(Project.class)))
                .thenReturn(project);

        Mockito.when(messageRepository.findByRecipientAndSeenDateIsNull(user))
                .thenReturn(new ArrayList<>());

        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(java.util.Optional.of(user));

        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(java.util.Optional.of(user1));

        Mockito.when(userRepository.findByUsernameIn(Collections.singletonList(user1.getUsername())))
                .thenReturn(Collections.singleton(user1));

        Mockito.when(teamRoleRepository.findByTeamAndAndUser(team, user))
                .thenReturn(java.util.Optional.of(teamRole));

        Mockito.when(teamRoleRepository.findById(teamRole.getId()))
                .thenReturn(java.util.Optional.of(teamRole));

        Mockito.when(teamRoleRepository.findByTeamAndAndUser(team, user1))
                .thenReturn(java.util.Optional.of(teamRole2));

        Mockito.when(teamRoleRepository.findById(teamRole2.getId()))
                .thenReturn(java.util.Optional.of(teamRole2));
    }

    @Test
    @WithMockUser
    public void givenGetRequestOnMyTeams_shouldReturnMyTeamRoles() throws Exception {
        mockMvc.perform(get("/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    public void givenPostRequestOnAddTeam_shouldReturnOk() throws Exception {
        team.setTeamRoles(null);
        mockMvc.perform(post("/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(team)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenGetRequestOnAddTeamProject_shouldReturnTeamRoles() throws Exception {
        mockMvc.perform(get("/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser
    public void givenGetRequestOnAddTeamProjectForSpecifiedTeam_shouldRetunTeamName() throws Exception {
        mockMvc.perform(get("/teams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString());
    }

    @Test
    @WithMockUser
    public void givenPostRequestOnAddTeamProject_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/teams/1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(newProject)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenGetRequestOnManageTeam_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/teams/1?roles=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team").isNotEmpty())
                .andExpect(jsonPath("$.currentUser").isNotEmpty())
                .andExpect(jsonPath("$.teamRoles").isNotEmpty());
    }

    @Test
    @WithMockUser
    public void givenPostRequestOnChangeName_shouldReturnOk() throws Exception {
        mockMvc.perform(patch("/teams/1/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"New name\"}"))
                .andExpect(status().isOk());
    }

    // TODO: 6/28/19 Check if user has been added
    @Test
    @WithMockUser
    public void givenPostRequestOnAddUsersToTeam_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/teams/1/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new Gson().toJson(Collections.singletonList("user1"))))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser
    public void givenRequestOnProjectList_shouldReturnMap() throws Exception {
        mockMvc.perform(get("/teams/1/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value(team.getName()))
                .andExpect(jsonPath("$.projects").exists())
                .andExpect(jsonPath("$.isCurrentUserTeamManager").exists());
    }

    @Test
    @WithMockUser
    public void givenRequestOnLeaveTeamPost_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/teams/1/leave"))
                .andExpect(status().isOk());
    }

    // TODO Move this two methods to separate class
    @Test
    @WithMockUser
    public void givenRequestOnDeleteTeamRole_shouldReturnOk() throws Exception {
        mockMvc.perform(delete("/teamRoles/4"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void givenRequestOnChangeTeamRole_shouldReturnOk() throws Exception {
        mockMvc.perform(patch("/teamRoles/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + TeamRoles.MEMBER + "\"}"))
                .andExpect(status().isOk());
    }
}
