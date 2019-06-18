package com.projecty.projectyweb.helpers;

import com.projecty.projectyweb.model.Project;
import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.RoleRepository;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserHelperImpl implements UserHelper {
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserHelperImpl(UserService userService, UserRepository userRepository, RoleRepository roleRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    private List<String> removeDuplicateUsernames(List<String> usernames) {
        return usernames.stream().distinct().collect(Collectors.toList());
    }

    private List<String> removeCurrentUserUsernameFromUsernamesList(List<String> usernames) {
        User current = userService.getCurrentUser();
        usernames.removeIf(current.getUsername()::equals);
        return usernames;
    }

    @Override
    public List<String> cleanUsernames(List<String> usernames) {
        usernames = removeDuplicateUsernames(usernames);
        return removeCurrentUserUsernameFromUsernamesList(usernames);
    }

    @Override
    public List<String> removeExistingUsernamesInProject(List<String> usernames, Project project) {
        List<String> newUsernames = new ArrayList<>();
        for (String username : usernames) {
            User user = userRepository.findByUsername(username);
            if (roleRepository.findRoleByUserAndProject(user, project) == null) {
                newUsernames.add(username);
            }
        }
        return newUsernames;
    }
}
