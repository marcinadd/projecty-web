package com.projecty.projectyweb.message;

import com.projecty.projectyweb.user.User;
import com.projecty.projectyweb.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindingResultUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class MessageValidator implements ConstraintValidator<ValidMessage, Message> {

    private final UserRepository userRepository;
    private final MessageSource messageSource;

    public MessageValidator(UserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    @Override
    public void initialize(ValidMessage constraintAnnotation) {

    }

    @Override
    public boolean isValid(Message value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        boolean isValid = true;
        if (value.getText() == null || StringUtils.isEmpty(value.getText())) {
            context
                    .buildConstraintViolationWithTemplate(messageSource.getMessage("message.title.empty", null, LocaleContextHolder.getLocale()))
                    .addPropertyNode("title")
                    .addConstraintViolation();
            isValid = false;
        }
        if (value.getTitle() == null || StringUtils.isEmpty(value.getTitle())) {
            context
                    .buildConstraintViolationWithTemplate(messageSource.getMessage("message.text.empty", null, LocaleContextHolder.getLocale()))
                    .addPropertyNode("text")
                    .addConstraintViolation();
            isValid = false;
        }

        Optional<User> recipient = userRepository.findByUsername(value.getRecipientUsername());

        if (!recipient.isPresent()) {
            context
                    .buildConstraintViolationWithTemplate(messageSource.getMessage("message.recipient.invalid", null, LocaleContextHolder.getLocale()))
                    .addPropertyNode("recipient")
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
