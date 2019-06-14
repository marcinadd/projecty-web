package com.projecty.projectyweb.service.user;

import com.projecty.projectyweb.model.User;
import org.springframework.validation.BindingResult;


public interface UserService {
    void saveWithPasswordEncrypt(User user);
    User getCurrentUser();
    boolean checkIfPasswordMatches(User user, String password);

    void validateExistingUser(User user, BindingResult bindingResult);
    void validateNewUser(User user, BindingResult bindingResult);

    BindingResult authUserAndValidatePassword(
            User user,
            String currentPassword,
            String newPassword,
            String repeatPassword
    );
}
