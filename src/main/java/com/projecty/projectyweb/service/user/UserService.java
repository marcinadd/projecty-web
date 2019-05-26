package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;


public interface UserService {
    void save(User user);

    User getCurrentUser();
}
