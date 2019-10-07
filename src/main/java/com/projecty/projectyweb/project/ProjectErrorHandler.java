package com.projecty.projectyweb.project;

import com.projecty.projectyweb.misc.ApiError;
import com.projecty.projectyweb.project.role.NoAdminsInProjectException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

@ControllerAdvice
public class ProjectErrorHandler {
    private final MessageSource messageSource;

    public ProjectErrorHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(NoAdminsInProjectException.class)
    public ResponseEntity<Object> handleNoAdminsInProjectException() {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,
                messageSource.getMessage(
                        "project.no_admins_in_project_exception",
                        null,
                        Locale.getDefault()
                ));
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }
}
