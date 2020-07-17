package com.projecty.projectyweb.settings;

import com.projecty.projectyweb.ProjectyWebApplication;
import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectyWebApplication.class)
public class SettingsServiceTests {
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private UserRepository userRepository;
    private static final String USERNAME_1 = "settingsServiceUsername1";
    private static final String USERNAME_2 = "settingsServiceUsername2";
    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(USERNAME_1)
    public void whenGetSettingsWhichDoesNotExist_shouldReturnNewSettings() {
        userRepository.save(User.builder().username(USERNAME_1).build());
        Settings settings = settingsService.getSettingsForCurrentUser();
        assertThat(settings.getId(), is(notNullValue()));
        assertTrue(settings.getCanBeAddedToProject());
    }

    @Test
    @WithMockUser(USERNAME_2)
    public void whenPatchSettings_shouldReturnPatchedSettings() {
        userRepository.save(User.builder().username(USERNAME_2).build());
        Settings patched = Settings.builder()
                .canBeAddedToProject(false)
                .canBeAddedToTeam(false)
                .isMessageEmailNotificationsEnabled(false)
                .isProjectEmailNotificationsEnabled(false)
                .isTeamEmailNotificationsEnabled(false)
                .build();
        settingsService.patchSettings(patched);
        User user = userService.getCurrentUser();
        assertFalse(user.getSettings().getCanBeAddedToProject());
        assertFalse(user.getSettings().getCanBeAddedToTeam());
        assertFalse(user.getSettings().getIsTeamEmailNotificationsEnabled());
        assertFalse(user.getSettings().getIsProjectEmailNotificationsEnabled());
        assertFalse(user.getSettings().getIsMessageEmailNotificationsEnabled());
        assertFalse(userService.getCurrentUser().getSettings().getCanBeAddedToProject());
    }
}
