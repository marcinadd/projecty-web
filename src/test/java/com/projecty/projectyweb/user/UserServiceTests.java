package com.projecty.projectyweb.user;

import com.projecty.projectyweb.ProjectyWebApplication;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class UserServiceTests {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Before
    public void init() {
        userRepository.save(User.builder().username("user").build());
    }


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void whenCreateNewUser_shouldReturnNewUser() {
        String username = "newUser";
        User user = userService.createUserAndGet(username);
        assertThat(user.getUsername(), is(username));
        assertThat(user.getProjectRoles(), is(notNullValue()));
        assertThat(userRepository.findById(user.getId()).get(), is(user));
    }

    @Test
    @WithMockUser
    public void getUsernameStartWith() {
        String username1 = "userX";
        String username2 = "auser";
        String username3 = "userY";
        userRepository.save(User.builder().username(username1).build());
        userRepository.save(User.builder().username(username2).build());
        userRepository.save(User.builder().username(username3).build());
        List<String> usernames = userService.getUsernamesStartWith("user");
        assertTrue(usernames.contains(username1));
        assertTrue(usernames.contains(username3));
        assertFalse(usernames.contains(username2));
        assertFalse(usernames.contains("user"));
    }
}
