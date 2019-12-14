package com.projecty.projectyweb.team;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class TeamServiceTests {
    @Autowired
    private TeamService teamService;

    @Autowired
    private TeamRoleRepository teamRoleRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    private User user;

    @Before
    public void init() {
        user = new User();
        user.setUsername("user");
        userRepository.save(user);
        Mockito.when(userService.getCurrentUser())
                .thenReturn(user);
    }

    @Test
    public void whenChangeName_shouldReturnTeamWithNewName() {
        Team team = new Team();
        team.setName("Old name");
        Map<String, String> fields = new HashMap<>();
        fields.put("name", "test");
        teamService.editTeam(team, fields);
        Optional<Team> team1 = teamRepository.findById(team.getId());
        if (team1.isPresent()) {
            assertThat(team1.get().getName(), is("test"));
        } else {
            assert false;
        }
    }

    @Test
    @Transactional
    public void whenCreateNewTeamWithoutOtherRoles_shouldReturnTeamWithOnlyOneRole() {
        Team team1 = new Team();
        team1.setName("My team name");
        team1 = teamService.createTeamAndSave(team1, null);
        assertThat(team1, is(notNullValue()));
        assertThat(teamRoleRepository.findByTeamAndAndUser(team1, user), is(notNullValue()));
    }

    @Test
    public void whenAddProject_shouldReturnTeamWithProject() {
        Optional<Team> team1 = teamRepository.findById(1L);
        Project project = new Project();
        project.setName("Sample project");
        team1.ifPresent(team -> teamService.createProjectForTeam(team, project));
        team1 = teamRepository.findById(1L);
        team1.ifPresent(team -> assertThat(team.getProjects(), is(notNullValue())));
    }
}
