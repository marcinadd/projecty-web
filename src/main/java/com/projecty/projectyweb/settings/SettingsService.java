package com.projecty.projectyweb.settings;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import com.projecty.projectyweb.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private final UserService userService;
    private final UserRepository userRepository;

    public SettingsService(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
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
}
