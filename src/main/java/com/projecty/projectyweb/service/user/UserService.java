package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;

import java.util.List;


public interface UserService {
    void save(User user);

    User findByUsername(String username);

    List<User> findByUsernames(List<String> usernames);

    User getCurrentUser();

}
