package com.projecty.projectyweb.project;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.dto.ProjectRoleData;
import com.projecty.projectyweb.role.Roles;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class ProjectServiceTests {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String USERNAME_1 = "projectServiceUser1";
    private static final String USERNAME_2 = "projectServiceUser2";
    private static final String USERNAME_3 = "projectServiceUser3";

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    @WithMockUser(USERNAME_1)
    @Transactional
    public void whenInviteToProjectByUsernames_shouldReturnSavedProjectRoles() {
        userRepository.save(User.builder().username(USERNAME_1).build());
        userRepository.save(User.builder().username(USERNAME_3).build());
        Project project = new Project();
        project = projectRepository.save(project);
        List<String> usernames = Collections.singletonList(USERNAME_3);
        List<ProjectRole> savedRoles = projectService.addProjectRolesByUsernames(project, usernames);
        assertThat(savedRoles.size(), is(1));
        assertThat(projectRepository.findById(project.getId()).get().getProjectRoleInvitations().size(), is(1));
    }

    @Test
    @WithMockUser(USERNAME_2)
    public void whenGetProjectRoleForCurrentUserByProjectId_shouldReturnProjectRoleData() {
        userRepository.save(User.builder().username(USERNAME_2).build());
        Project project = projectService.createNewProjectAndSave(new Project(), null);
        ProjectRoleData projectRoleData = projectService.getProjectRoleForCurrentUserByProjectId(project.getId());
        assertThat(projectRoleData.getProject().getId(), Is.is(project.getId()));
        assertThat(projectRoleData.getName(), Is.is(Roles.MANAGER));
    }
}
