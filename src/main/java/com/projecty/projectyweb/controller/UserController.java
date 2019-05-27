package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.service.user.UserService;
import com.projecty.projectyweb.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;


@Controller
public class UserController {
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserService userService;

    @RequestMapping("register")
    public ModelAndView register() {
        return new ModelAndView("fragments/register", "user", new User());
    }

    @PostMapping("register")
    public String registerPost(@Valid @ModelAttribute User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "fragments/register";
        }
        userService.save(user);
        return "redirect:/project/myprojects";
    }

    @GetMapping("login")
    public String login() {
        return "fragments/login";
    }

    @GetMapping("index")
    public String index() {
        return "index";
    }
}
