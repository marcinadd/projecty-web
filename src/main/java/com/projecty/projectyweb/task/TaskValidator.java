package com.projecty.projectyweb.task;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class TaskValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Task task = (Task) target;
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "name.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDate", "startDate.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDate", "endDate.empty");

        if (task.getStartDate() != null && task.getEndDate() != null && task.getStartDate().after(task.getEndDate())) {
            errors.rejectValue("startDate", "start.date.greater.than.end.date");
        }
    }
}
