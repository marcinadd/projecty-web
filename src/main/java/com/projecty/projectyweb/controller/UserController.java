package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.service.user.UserService;
import com.projecty.projectyweb.validator.UserValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;


@Controller
public class UserController {
    private final UserValidator userValidator;
    private final UserService userService;

    public UserController(UserValidator userValidator, UserService userService) {
        this.userValidator = userValidator;
        this.userService = userService;
    }

    @RequestMapping("register")
    public ModelAndView register() {
        return new ModelAndView("fragments/user/register", "user", new User());
    }

    @PostMapping("register")
    public String registerPost(@Valid @ModelAttribute User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "fragments/user/register";
        }
        userService.saveWithPasswordEncrypt(user);
        return "redirect:/project/myprojects";
    }

    @GetMapping("login")
    public String login() {
        return "fragments/user/login";
    }

    @GetMapping("index")
    public String index() {
        return "index";
    }

    @GetMapping("settings")
    public ModelAndView settings() {
        return new ModelAndView("fragments/user/settings", "user", userService.getCurrentUser());
    }

    @PostMapping("changePassword")
    public String changePasswordPost(
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String repeatPassword

    ) {
        User user = userService.getCurrentUser();
        if (userService.checkIfPasswordMatches(user, currentPassword)) {
            user.setPassword(newPassword);
            user.setPasswordRepeat(repeatPassword);
            BindingResult results = userService.validate(user);
            if (results.getErrorCount() == 1 && results.getFieldErrors("username").size() == 1) {
                userService.saveWithPasswordEncrypt(user);
                return "redirect:/settings";
            }
        } else {
            ObjectError objectError = new ObjectError("password", "Wrong current password");
            //bindingResult.addError(objectError);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
