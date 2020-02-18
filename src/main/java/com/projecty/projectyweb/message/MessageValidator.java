package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.*;

import java.util.Optional;

@Component
public class MessageValidator implements Validator {

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return Message.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "text", "message.text.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "message.title.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "recipientUsername", "message.recipient.invalid");

        Message message = (Message) target;

        Optional<User> recipient = userService.findByByUsername(message.getRecipientUsername());

        if (!recipient.isPresent()) {
            errors.rejectValue("recipientUsername", "message.recipient.invalid");
        } else {
            User sender = userService.getCurrentUser();
            if (sender == recipient.get()) {
                errors.rejectValue("recipientUsername", "message.recipient.yourself");
           }
        }
    }
}
