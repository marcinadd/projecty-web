package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;

import java.util.List;
import java.util.Optional;


public interface UserService {
    void save(User user);

    User findByUsername(String username);

    List<User> findByUsernames(List<String> usernames);

    Optional<User> findById(Long id);

    User getCurrentUser();
}
