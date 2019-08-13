package com.projecty.projectyweb.user;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

@CrossOrigin()
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public void registerPost(
            @Valid @ModelAttribute RegisterForm registerForm,
            BindingResult bindingResult
    ) throws BindException {
        User user = userService.createUserFromRegisterForm(registerForm);
        userService.validateNewUser(user, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        userService.saveWithPasswordEncrypt(user);
    }

    @GetMapping("settings")
    public User settings() {
        return userService.getCurrentUser();
    }

    @PostMapping("changePassword")
    public void changePasswordPost(
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String repeatPassword

    ) throws BindException {
        User user = userService.getCurrentUser();
        BindingResult bindingResult = userService.authUserAndValidatePassword(user, currentPassword, newPassword, repeatPassword);
        if (bindingResult == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        } else if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        userService.saveWithPasswordEncrypt(user);
    }

    @GetMapping("auth")
    public User getUser() {
        return userService.getCurrentUser();
    }
}
