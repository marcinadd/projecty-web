package com.projecty.projectyweb.user;

import com.projecty.projectyweb.misc.RedirectMessage;
import com.projecty.projectyweb.misc.RedirectMessageTypes;
import com.projecty.projectyweb.project.Project;
import com.projecty.projectyweb.role.RoleRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Deprecated
public class UserHelper {
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MessageSource messageSource;

    public UserHelper(UserService userService, UserRepository userRepository, RoleRepository roleRepository, MessageSource messageSource) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.messageSource = messageSource;
    }

    private List<String> removeDuplicateUsernames(List<String> usernames) {
        return usernames.stream().distinct().collect(Collectors.toList());
    }

    private List<String> removeCurrentUserUsernameFromUsernamesList(List<String> usernames) {
        User current = userService.getCurrentUser();
        usernames.removeIf(current.getUsername()::equals);
        return usernames;
    }

    public List<String> cleanUsernames(List<String> usernames) {
        usernames = removeDuplicateUsernames(usernames);
        return removeCurrentUserUsernameFromUsernamesList(usernames);
    }

    public List<String> removeExistingUsernamesInProject(List<String> usernames, Project project, List<RedirectMessage> messages) {
        List<String> newUsernames = new ArrayList<>();
        for (String username : usernames) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent() && !roleRepository.findRoleByUserAndProject(user.get(), project).isPresent()) {
                newUsernames.add(username);
            } else {
                RedirectMessage message = new RedirectMessage();
                message.setType(RedirectMessageTypes.FAILED);
                String text = messageSource.getMessage(
                        "role.add.exists",
                        new Object[]{username},
                        Locale.getDefault());
                message.setText(text);
                messages.add(message);
            }
        }
        return newUsernames;
    }
}
