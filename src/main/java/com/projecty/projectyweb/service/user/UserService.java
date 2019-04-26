package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;


public interface UserService {
    void save(User user);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByUsernameAndPassword();

}
