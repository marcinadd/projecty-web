package com.projecty.projectyweb.user;

import com.projecty.projectyweb.user.avatar.Avatar;
import com.projecty.projectyweb.user.avatar.AvatarService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserValidator userValidator;

    private final AvatarService avatarService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidator userValidator, AvatarService avatarService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
        this.avatarService = avatarService;
    }

    public void saveWithPasswordEncrypt(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public User getCurrentUser() {
        Object currentUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = null;
        if (currentUser instanceof UserDetails) {
            currentUsername = ((UserDetails) currentUser).getUsername();
        }
        if (userRepository.findByUsername(currentUsername).isPresent()) {
            return userRepository.findByUsername(currentUsername).get();
        }
        return null;
    }

    private boolean checkIfPasswordMatches(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public void validateExistingUser(User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
    }

    public void validateNewUser(User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            bindingResult.rejectValue("username", "exists.user.username");
        }
    }

    public BindingResult authUserAndValidatePassword(
            User user,
            String currentPassword,
            String newPassword,
            String repeatPassword
    ) {
        if (checkIfPasswordMatches(user, currentPassword)) {
            user.setPassword(newPassword);
            user.setPasswordRepeat(repeatPassword);
            DataBinder dataBinder = new DataBinder(user);
            dataBinder.setValidator(userValidator);
            dataBinder.validate();
            return dataBinder.getBindingResult();
        }
        return null;
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

    User createUserFromRegisterForm(RegisterForm registerForm) {
        return new UserBuilder()
                .username(registerForm.getUsername())
                .email(registerForm.getEmail())
                .password(registerForm.getPassword())
                .passwordRepeat(registerForm.getPasswordRepeat())
                .avatar(registerForm.getAvatar())
                .build();
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
}
