package com.projecty.projectyweb.user;

import com.projecty.projectyweb.ProjectyWebApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class UserServiceTests {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void whenCreateNewUser_shouldReturnNewUser() {
        String username = "newUser";
        User user = userService.createUserAndGet(username);
        assertThat(user.getUsername(), is(username));
        assertThat(user.getProjectRoles(), is(notNullValue()));
        assertThat(userRepository.findById(user.getId()).get(), is(user));
    }
}
