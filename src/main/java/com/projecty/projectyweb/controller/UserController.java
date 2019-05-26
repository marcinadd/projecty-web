package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.service.user.UserService;
import com.projecty.projectyweb.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;


@Controller
//@RequestMapping("user")
public class UserController {
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserService userService;

    @RequestMapping("register")
    public String welcome(Model model) {
        model.addAttribute("user", new User());
        return "fragments/register";
    }

    @PostMapping("register")
    public String register(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model) {
        System.out.println("ok");
        userValidator.validate(user, bindingResult);

        if (bindingResult.hasErrors()) {
            System.out.println("errrors");
            return "fragments/register";
        }
        userService.save(user);
        System.out.println("Validation ok");
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
