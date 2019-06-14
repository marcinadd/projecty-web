package com.projecty.projectyweb.controller;

import com.projecty.projectyweb.model.User;
import com.projecty.projectyweb.service.user.UserService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Locale;

import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_FAILED;
import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_SUCCESS;


@Controller
public class UserController {
    private final UserService userService;
    private final MessageSource messageSource;

    public UserController(UserService userService, MessageSource messageSource) {
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @RequestMapping("register")
    public ModelAndView register() {
        return new ModelAndView("fragments/user/register", "user", new User());
    }

    @PostMapping("register")
    public String registerPost(@Valid @ModelAttribute User user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        userService.validateNewUser(user, bindingResult);
        if (bindingResult.hasErrors()) {
            return "fragments/user/register";
        }
        userService.saveWithPasswordEncrypt(user);
        redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("user.register.success", new Object[]{user.getUsername()}, Locale.getDefault())));
        return "redirect:/login";
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
            @RequestParam(required = false) String repeatPassword,
            RedirectAttributes redirectAttributes

    ) {
        User user = userService.getCurrentUser();
        BindingResult bindingResult = userService.authUserAndValidatePassword(user, currentPassword, newPassword, repeatPassword);
        if (bindingResult == null) {
            redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_FAILED, Collections.singletonList(messageSource.getMessage("user.password.authorize.failed", null, Locale.getDefault())));
            return "redirect:/settings";
        } else if (bindingResult.hasErrors()) {
            return "fragments/user/settings";
        }
        userService.saveWithPasswordEncrypt(user);
        redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("user.password.change.success", null, Locale.getDefault())));
        return "redirect:/settings";
    }
}
