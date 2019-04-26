package com.projecty.projectyweb.validator;

import com.projecty.projectyweb.model.User;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return User.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "username.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "email.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "password.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "passwordRepeat", "passwordRepeat.empty");

        User user = (User) target;

        if (!user.getPassword().equals(user.getPasswordRepeat())) {
            errors.rejectValue("passwordRepeat", "diff.user.passwordRepeat");
        }


        //TODO Add user validation


    }
}
