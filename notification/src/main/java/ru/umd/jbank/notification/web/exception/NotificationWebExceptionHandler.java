package ru.umd.jbank.notification.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NotificationWebExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleInsufficientBalance(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Notification error: " + e.getMessage());
    }
}
