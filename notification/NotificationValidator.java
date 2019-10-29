package com.projecty.projectyweb.notification;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class NotificationValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return Notification.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Notification notif = (Notification) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "timeStamp", "timeStamp.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "msgCode", "msgCode.empty");
    }
}
