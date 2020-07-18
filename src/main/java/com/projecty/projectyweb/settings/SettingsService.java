package com.projecty.projectyweb.settings;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private final UserService userService;
    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;

    public SettingsService(UserService userService, UserRepository userRepository, SettingsRepository settingsRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
    }

    public Settings getSettingsForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getSettings() != null) {
            return currentUser.getSettings();
        }
        return addSettingsForUser(currentUser);
    }

    private Settings addSettingsForUser(User user) {
        user.setSettings(new Settings());
        user = userRepository.save(user);
        return user.getSettings();
    }

    public Settings patchSettings(Settings patchedSettings) {
        User currentUser = userService.getCurrentUser();
        Settings settings = currentUser.getSettings() != null ? currentUser.getSettings() : addSettingsForUser(currentUser);

        // TODO Change this ugly code
        Boolean isMessageEmailNotificationsEnabled = patchedSettings.getIsMessageEmailNotificationEnabled();
        if (isMessageEmailNotificationsEnabled != null) {
            settings.setIsMessageEmailNotificationEnabled(isMessageEmailNotificationsEnabled);
        }
        Boolean isEmailNotificationsEnabled = patchedSettings.getIsEmailNotificationEnabled();
        if (isEmailNotificationsEnabled != null) {
            settings.setIsEmailNotificationEnabled(isEmailNotificationsEnabled);
        }
        Boolean canBeAddedToProject = patchedSettings.getCanBeAddedToProject();
        if (canBeAddedToProject != null) {
            settings.setCanBeAddedToProject(canBeAddedToProject);
        }
        Boolean canBeAddedToTeam = patchedSettings.getCanBeAddedToTeam();
        if (canBeAddedToTeam != null) {
            settings.setCanBeAddedToTeam(canBeAddedToTeam);
        }
        return settingsRepository.save(settings);
    }
}
