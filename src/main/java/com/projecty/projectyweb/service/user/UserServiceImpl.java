package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


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
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findByEmail(String email) {
        return null;
    }

    @Override
    public User findByUsernameAndPassword() {
        return null;
    }

    @Override
    public List<User> findByUsernames(List<String> usernames) {
        List<User> users = new ArrayList<>();

        for (String username : usernames
        ) {
            User user = userRepository.findByUsername(username);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User getCurrentUser() {
        Object currentUser = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = null;
        if (currentUser instanceof UserDetails) {
            currentUsername = ((UserDetails) currentUser).getUsername();
        }

        return findByUsername(currentUsername);
    }


}
