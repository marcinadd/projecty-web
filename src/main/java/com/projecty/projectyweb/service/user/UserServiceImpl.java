package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void save(User user) {
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
}
