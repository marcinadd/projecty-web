package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.UserRepository;
import com.projecty.projectyweb.validator.UserValidator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserValidator userValidator;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userValidator = userValidator;
    }

    @Override
    public void saveWithPasswordEncrypt(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    @Override
    public User getCurrentUser() {
        Object currentUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = null;
        if (currentUser instanceof UserDetails) {
            currentUsername = ((UserDetails) currentUser).getUsername();
        }
        return userRepository.findByUsername(currentUsername);
    }

    @Override
    public boolean checkIfPasswordMatches(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    @Override
    public BindingResult validateExistingUser(User user) {
        DataBinder dataBinder = new DataBinder(user);
        dataBinder.setValidator(userValidator);
        dataBinder.validate();
        return dataBinder.getBindingResult();
    }

    @Override
    public void validateNewUser(User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
        if (userRepository.findByUsername(user.getUsername()) != null) {
            bindingResult.rejectValue("username", "exists.user.username");
        }
    }
}
