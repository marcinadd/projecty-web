package com.projecty.projectyweb.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;


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
}
