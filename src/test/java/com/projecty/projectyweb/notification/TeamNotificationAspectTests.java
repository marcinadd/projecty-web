package com.projecty.projectyweb.notification;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.notifications.NotificationRepository;
import com.projecty.projectyweb.team.Team;
import com.projecty.projectyweb.team.TeamNotificationAspect;
import com.projecty.projectyweb.team.TeamRepository;
import com.projecty.projectyweb.team.role.TeamRole;
import com.projecty.projectyweb.team.role.TeamRoleRepository;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class TeamNotificationAspectTests {
    @Autowired
    private TeamNotificationAspect teamNotificationAspect;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamRoleRepository teamRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @WithMockUser("aspectCurrentUser")
    @Transactional
    public void whenCreateTeam_shouldSendNotifications() {
        Team team = teamRepository.save(new Team());
        User savedUser = userRepository.save(User.builder().username("aspectTeamUser").build());
        TeamRole teamRole = new TeamRole();
        teamRole.setTeam(team);
        teamRole.setUser(savedUser);
        teamRole = teamRoleRepository.save(teamRole);
        team.setTeamRoles(Collections.singletonList(teamRole));
        team = teamRepository.findById(team.getId()).get();
        teamNotificationAspect.afterNewTeamCreated(team);
        assertThat(notificationRepository.findByUser(savedUser).size(), is(1));
    }

    @Test
    @WithMockUser("aspectCurrentUser")
    public void whenAddRolesToExistingTeam_shouldSendNotifications() {
        Team team = teamRepository.save(new Team());
        User savedUser = userRepository.save(User.builder().username("otherTeamNotificationUser").build());
        TeamRole teamRole = new TeamRole();
        teamRole.setTeam(team);
        teamRole.setUser(savedUser);
        teamRole = teamRoleRepository.save(teamRole);
        List<TeamRole> teamRoles = new ArrayList<>();
        teamRoles.add(teamRole);
        teamNotificationAspect.afterTeamRolesAdded(teamRoles);
        assertThat(notificationRepository.findByUser(savedUser).size(), is(1));
    }
}
