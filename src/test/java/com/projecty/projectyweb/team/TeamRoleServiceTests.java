package com.projecty.projectyweb.team;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.team.role.*;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class TeamRoleServiceTests {

    @Autowired
    private TeamRoleService teamRoleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamRoleRepository teamRoleRepository;

    @MockBean
    private UserService userService;

    private User user;
    private User user1;

    @Before
    public void init() {
        user = new User();
        user.setUsername("user");
        userRepository.save(user);

        user1 = new User();
        user1.setUsername("user1");
        userRepository.save(user1);

        Mockito.when(userService.getCurrentUser())
                .thenReturn(user);
    }

    @Test
    public void whenAddTeamRolesByUsernames_shouldReturnTeamWithTeamRoles() {
        Team team = new Team();
        team.setName("Sample team");
        team = teamRepository.save(team);
        List<String> usernames = new ArrayList<>();
        usernames.add(user.getUsername());
        usernames.add(user.getUsername());
        usernames.add(user1.getUsername());
        usernames.add(user1.getUsername());
        teamRoleService.addTeamMembersByUsernames(team, usernames, null);

        Optional<Team> optionalTeam = teamRepository.findById(team.getId());
        if (optionalTeam.isPresent()) {
            assertThat(teamRoleRepository.findByTeamAndAndUser(optionalTeam.get(), user), is(notNullValue()));
            assertThat(teamRoleRepository.findByTeamAndAndUser(optionalTeam.get(), user1), is(notNullValue()));
        } else {
            assert false;
        }
    }

    @Test
    public void whenAddCurrentUserAsTeamManager_shouldReturnTeamWithCurrentUserAsTeamManager() {
        Team team = new Team();
        team.setName("Sample");
        teamRoleService.addCurrentUserAsTeamManager(team);
        teamRepository.save(team);

        Optional<Team> optionalTeam = teamRepository.findById(team.getId());
        if (optionalTeam.isPresent() && teamRoleRepository.findByTeamAndAndUser(optionalTeam.get(), user).isPresent()) {
            assertThat(
                    teamRoleRepository.findByTeamAndAndUser(optionalTeam.get(), user).get().getName(),
                    is(TeamRoles.MANAGER));
        } else {
            assert false;
        }
    }

    @Test
    public void whenCurrentUserIsProjectManager_shouldReturnTrue() {
        Team team = new Team();
        team.setName("Sample");
        TeamRole teamRole = new TeamRole();
        teamRole.setUser(user);
        teamRole.setTeam(team);
        teamRole.setName(TeamRoles.MANAGER);
        team.setTeamRoles(Collections.singletonList(teamRole));
        teamRepository.save(team);

        assertThat(teamRoleService.isCurrentUserTeamManager(team), is(true));
    }

    @Test
    public void whenCurrentUserIsNotProjectManager_shouldReturnFalse() {
        Team team = new Team();
        team.setName("Sample");
        TeamRole teamRole = new TeamRole();
        teamRole.setUser(user);
        teamRole.setTeam(team);
        teamRole.setName(TeamRoles.MEMBER);
        team.setTeamRoles(Collections.singletonList(teamRole));
        teamRepository.save(team);

        assertThat(teamRoleService.isCurrentUserTeamManager(team), is(false));
    }

    @Test
    public void whenChangeTeamRole_shouldReturnRoleWithNewName() {
        TeamRole teamRole = new TeamRole();
        teamRole.setName(TeamRoles.MANAGER);
        teamRoleService.changeTeamRole(teamRole, String.valueOf(TeamRoles.MEMBER));
        assertThat(teamRole.getName(), is(TeamRoles.MEMBER));
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenChangeTeamRoleOnNotExists_shouldThrowException() {
        TeamRole teamRole = new TeamRole();
        teamRole.setName(TeamRoles.MANAGER);
        teamRoleService.changeTeamRole(teamRole, "NotExistsRoleName");
    }

    @Test
    @Transactional
    public void whenLeaveTeamWithOtherManagers_shouldReturnTeamWithoutUserRole() throws NoManagersInTeamException {
        User current = new User();
        current.setUsername("current");
        current = userRepository.save(current);

        User manager = new User();
        manager.setUsername("manager");
        manager = userRepository.save(manager);

        Team team = new Team();
        team.setName("sample-team");
        team = teamRepository.save(team);

        TeamRole managerRole = new TeamRole();
        managerRole.setTeam(team);
        managerRole.setUser(manager);
        managerRole.setName(TeamRoles.MANAGER);
        managerRole = teamRoleRepository.save(managerRole);

        TeamRole currentRole = new TeamRole();
        currentRole.setTeam(team);
        currentRole.setUser(current);
        currentRole.setName(TeamRoles.MEMBER);
        currentRole = teamRoleRepository.save(currentRole);

        List<TeamRole> teamRoles = new ArrayList<>();
        teamRoles.add(managerRole);
        teamRoles.add(currentRole);
        team.setTeamRoles(teamRoles);
        teamRepository.save(team);
        teamRoleService.leaveTeam(team, current);
        Optional<Team> optionalTeam = teamRepository.findById(team.getId());
        if (optionalTeam.isPresent()) {
            assertThat(optionalTeam.get().getTeamRoles().size(), is(1));
        } else {
            assert false;
        }
    }

    @Test(expected = NoManagersInTeamException.class)
    @Transactional
    public void whenLeaveTeamWithoutOtherManagers_shouldReturnTeamWithoutUserRole() throws NoManagersInTeamException {
        User current = new User();
        current.setUsername("current");
        current = userRepository.save(current);

        User manager = new User();
        manager.setUsername("manager");
        manager = userRepository.save(manager);

        Team team = new Team();
        team.setName("sample-team");
        team = teamRepository.save(team);

        TeamRole managerRole = new TeamRole();
        managerRole.setTeam(team);
        managerRole.setUser(manager);
        managerRole.setName(TeamRoles.MEMBER);
        managerRole = teamRoleRepository.save(managerRole);

        TeamRole currentRole = new TeamRole();
        currentRole.setTeam(team);
        currentRole.setUser(current);
        currentRole.setName(TeamRoles.MEMBER);
        currentRole = teamRoleRepository.save(currentRole);

        List<TeamRole> teamRoles = new ArrayList<>();
        teamRoles.add(managerRole);
        teamRoles.add(currentRole);
        team.setTeamRoles(teamRoles);
        teamRepository.save(team);
        teamRoleService.leaveTeam(team, current);
    }
}
