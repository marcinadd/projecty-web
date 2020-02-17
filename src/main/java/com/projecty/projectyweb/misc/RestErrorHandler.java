package com.projecty.projectyweb.misc;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class RestErrorHandler extends ResponseEntityExceptionHandler {
    private final MessageSource messageSource;

    public RestErrorHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers,
            HttpStatus status, WebRequest request) {
        String error = ex.getParameterName() + " parameter is missing";

        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, error);
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final List<String> errors = new ArrayList<>();
        
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> errors.add(messageSource.getMessage(fieldError, LocaleContextHolder.getLocale())));
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, errors);
        return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException exception) {
        final List<String> errors = new ArrayList<>();
        exception.getConstraintViolations().forEach(error -> errors.add(error.getPropertyPath() + ": "+error.getMessage()));
        final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, errors);
        return new ResponseEntity<ApiError>(apiError, null, HttpStatus.BAD_REQUEST);
    }
}
