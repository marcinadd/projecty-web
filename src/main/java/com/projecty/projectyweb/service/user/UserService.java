package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;
import org.springframework.validation.BindingResult;


public interface UserService {
    void saveWithPasswordEncrypt(User user);
    User getCurrentUser();
    boolean checkIfPasswordMatches(User user, String password);

    BindingResult validateExistingUser(User user);

    void validateNewUser(User user, BindingResult bindingResult);
}
