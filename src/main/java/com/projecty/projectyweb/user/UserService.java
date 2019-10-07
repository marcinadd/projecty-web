package com.projecty.projectyweb.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserValidator userValidator;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
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
        for (User user : users
        ) {
            usernames.add(user.getUsername());
        }
        return usernames;
    }

    User createUserFromRegisterForm(RegisterForm registerForm) {
        User user = new User();
        user.setUsername(registerForm.getUsername());
        user.setEmail(registerForm.getEmail());
        user.setPassword(registerForm.getPassword());
        user.setPasswordRepeat(registerForm.getPasswordRepeat());
        return user;
    }
}
