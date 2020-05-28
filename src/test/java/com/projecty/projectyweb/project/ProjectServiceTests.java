package com.projecty.projectyweb.project;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.project.role.ProjectRole;
import com.projecty.projectyweb.project.role.ProjectRoleRepository;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import org.junit.Before;
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
    private ProjectRoleRepository projectRoleRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @Before
    public void init() {
        user = userRepository.save(User.builder().username("projectUser").build());
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    @WithMockUser
    @Transactional
    public void whenAddRolesToProjectSByUsernames_shouldReturnSavedProjectRoles() {
        Project project = new Project();
        project = projectRepository.save(project);
        List<String> usernames = Collections.singletonList(user.getUsername());
        List<ProjectRole> savedRoles = projectService.addProjectRolesByUsernames(project, usernames);
        assertThat(savedRoles.size(), is(1));
        assertThat(projectRepository.findById(project.getId()).get().getProjectRoles().size(), is(1));
    }

}
