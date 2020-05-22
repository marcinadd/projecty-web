package com.projecty.projectyweb.user;

import com.projecty.projectyweb.user.avatar.Avatar;
import com.projecty.projectyweb.user.avatar.AvatarService;
import org.keycloak.KeycloakPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final AvatarService avatarService;

    public UserService(UserRepository userRepository, AvatarService avatarService) {
        this.userRepository = userRepository;
        this.avatarService = avatarService;
    }

    public User getCurrentUser() {
        String currentUsername = null;
        Object principal = SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        if (principal instanceof KeycloakPrincipal) {
            currentUsername = ((KeycloakPrincipal) principal).getName();
        } else if (principal instanceof UserDetails) {
            // Legacy code for testing
            //TODO Move this part of code to test package
            currentUsername = ((UserDetails) principal).getUsername();
        }
        Optional<User> user = userRepository.findByUsername(currentUsername);
        String finalCurrentUsername = currentUsername;
        return user.orElseGet(() -> userRepository.save(new UserBuilder().username(finalCurrentUsername).build()));
    }

    public Set<User> getUserSetByUsernames(List<String> usernames) {
        usernames = usernames.stream().distinct().collect(Collectors.toList());
        return userRepository.findByUsernameIn(usernames);
    }

    public Set<User> getUserSetByUsernamesWithoutCurrentUser(List<String> usernames) {
        Set<User> users = getUserSetByUsernames(usernames);
        users.remove(getCurrentUser());
        return users;
    }

    public List<String> getUsernamesFromUserList(List<User> users) {
        List<String> usernames = new ArrayList<>();
        users.forEach(user -> usernames.add(user.getUsername()));
        return usernames;
    }

    public void setUserAvatar(MultipartFile multipartFile) throws IOException, SQLException {
        if (!multipartFile.isEmpty()) {
            User current = getCurrentUser();
            Avatar currentAvatar = current.getAvatar();
            if (currentAvatar != null) {
                avatarService.delete(currentAvatar);
            }
            Avatar avatar = new Avatar();
            avatar.setContentType(multipartFile.getContentType());
            avatar.setFile(new SerialBlob(multipartFile.getBytes()));
            avatar.setUser(current);
            current.setAvatar(avatar);
            userRepository.save(current);
        }
    }

    public Optional<User> findByByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
