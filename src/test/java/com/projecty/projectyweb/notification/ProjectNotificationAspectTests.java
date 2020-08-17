package com.projecty.projectyweb.notification;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.notifications.NotificationRepository;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.project.ProjectNotificationAspect;
import com.projecty.projectyweb.project.ProjectRepository;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.role.Roles;
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
public class ProjectNotificationAspectTests {
    @Autowired
    private ProjectNotificationAspect projectNotificationAspect;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectRoleRepository projectRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @WithMockUser("aspectCurrentUser")
    @Transactional
    public void whenCreateProject_shouldSendNotifications() {
        Project project = projectRepository.save(new Project());
        User savedUser = userRepository.save(User.builder().username("aspectProjectUser1").build());
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setUser(savedUser);
        projectRole = projectRoleRepository.save(projectRole);
        project.setRoles(Collections.singletonList(projectRole));
        project = projectRepository.findById(project.getId()).get();
        projectNotificationAspect.afterNewProjectCreated(project);
        assertThat(notificationRepository.findByUser(savedUser).size(), is(1));
    }

    @Test
    @WithMockUser("aspectCurrentUser")
    public void whenAddRolesToExistingProject_shouldSendNotifications() {
        Project project = projectRepository.save(new Project());
        User savedUser = userRepository.save(User.builder().username("aspectProjectUser2").build());
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setUser(savedUser);
        projectRole = projectRoleRepository.save(projectRole);
        List<ProjectRole> projectRoles = new ArrayList<>();
        projectRoles.add(projectRole);
        projectNotificationAspect.afterProjectRolesAdded(projectRoles);
        assertThat(notificationRepository.findByUser(savedUser).size(), is(1));
    }

    @Test
    @WithMockUser("aspectCurrentUser")
    @Transactional
    public void whenChangeProjectRole_shouldSendNotifications() {
        Project project = projectRepository.save(new Project());
        User savedUser = userRepository.save(User.builder().username("aspectProjectUser3").build());
        ProjectRole projectRole = new ProjectRole();
        projectRole.setProject(project);
        projectRole.setUser(savedUser);
        projectRole.setName(Roles.MANAGER);
        projectRole = projectRoleRepository.save(projectRole);
        project.setRoles(Collections.singletonList(projectRole));
        projectNotificationAspect.afterProjectRolePatched(projectRole);
        assertThat(notificationRepository.findByUser(savedUser).size(), is(1));
    }
}
