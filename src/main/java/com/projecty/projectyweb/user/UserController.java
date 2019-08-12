package com.projecty.projectyweb.user;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Locale;

import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_FAILED;
import static com.projecty.projectyweb.configurations.AppConfig.REDIRECT_MESSAGES_SUCCESS;

@CrossOrigin()
@RestController
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
    public String registerPost(@Valid @ModelAttribute RegisterForm registerForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws BindException {
        User user = userService.createUserFromRegisterForm(registerForm);
        userService.validateNewUser(user, bindingResult);
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
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
    public User settings() {
        return userService.getCurrentUser();
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (bindingResult.hasErrors()) {
            return "fragments/user/settings";
        }
        userService.saveWithPasswordEncrypt(user);
        redirectAttributes.addFlashAttribute(REDIRECT_MESSAGES_SUCCESS, Collections.singletonList(messageSource.getMessage("user.password.change.success", null, Locale.getDefault())));
        return "redirect:/settings";
    }

    @GetMapping("auth")
    public User getUser() {
        return userService.getCurrentUser();
    }
}
