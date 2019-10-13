package com.projecty.projectyweb.team;

import com.projecty.projectyweb.misc.ApiError;
import com.projecty.projectyweb.team.role.NoManagersInTeamException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Locale;

@ControllerAdvice
public class TeamErrorHandler {
    private final MessageSource messageSource;

    public TeamErrorHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(NoManagersInTeamException.class)
    public ResponseEntity<Object> handleNoManagersInTeamException(NoManagersInTeamException ex) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST,
                messageSource.getMessage(
                        "team.no_managers_in_team_exception",
                        null,
                        LocaleContextHolder.getLocale()
                ));
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }
}
