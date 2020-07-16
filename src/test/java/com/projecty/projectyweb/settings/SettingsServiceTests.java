package com.projecty.projectyweb.settings;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class SettingsServiceTests {
    private static final String USERNAME_1 = "settingsServiceUsername1";
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser
    public void whenGetSettingsWhichDoesNotExist_shouldReturnNewSettings() {
        userRepository.save(User.builder().username(USERNAME_1).build());
        Settings settings = settingsService.getSettingsForCurrentUser();
        assertThat(settings.getId(), is(notNullValue()));
        assertTrue(settings.getCanBeAddedToProject());
    }

}
