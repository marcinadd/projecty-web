package com.projecty.projectyweb.message;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MessageValidator.class)
public @interface ValidMessage {
    String message() default "Invalid message.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
