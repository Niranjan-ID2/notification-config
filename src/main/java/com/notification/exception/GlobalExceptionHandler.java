package com.notification.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Catches defined custom exceptions and standard Spring exceptions to return appropriate HTTP responses.
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles {@link InvalidRequestException}.
     *
     * @param ex      The exception.
     * @param request The current web request.
     * @return A {@link ResponseEntity} with HTTP 400 Bad Request status.
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> handleInvalidRequestException(InvalidRequestException ex, WebRequest request) {
        log.warn("Invalid request: {}", ex.getMessage());
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getClass().getSimpleName());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    /**
     * Handles {@link EmailSendingException}.
     *
     * @param ex      The exception.
     * @param request The current web request.
     * @return A {@link ResponseEntity} with HTTP 500 Internal Server Error status.
     */
    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<Object> handleEmailSendingException(EmailSendingException ex, WebRequest request) {
        log.error("Email sending failed: {}", ex.getMessage(), ex);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while sending email.", ex.getClass().getSimpleName());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    /**
     * Handles {@link MethodArgumentNotValidException}, which occurs when @Valid validation fails.
     *
     * @param ex      The exception.
     * @param headers The headers.
     * @param status  The HTTP status.
     * @param request The current web request.
     * @return A {@link ResponseEntity} with HTTP 400 Bad Request status and validation error details.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation failed", errors);
        return new ResponseEntity<>(apiError, headers, status);
    }

    /**
     * Handles any other unhandled exceptions.
     *
     * @param ex      The exception.
     * @param request The current web request.
     * @return A {@link ResponseEntity} with HTTP 500 Internal Server Error status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception ex, WebRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later.", ex.getClass().getSimpleName());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    /**
     * Inner class to represent a structured API error response.
     */
    private static class ApiError {
        private HttpStatus status;
        private String message;
        private String error;
        private Map<String, String> fieldErrors;

        public ApiError(HttpStatus status, String message, String error) {
            this.status = status;
            this.message = message;
            this.error = error;
        }

        public ApiError(HttpStatus status, String message, Map<String, String> fieldErrors) {
            this.status = status;
            this.message = message;
            this.fieldErrors = fieldErrors;
        }

        // Getters are needed for Jackson serialization
        public HttpStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getError() {
            return error;
        }

        public Map<String, String> getFieldErrors() {
            return fieldErrors;
        }
    }
}
