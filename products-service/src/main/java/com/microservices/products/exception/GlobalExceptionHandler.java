package com.microservices.products.exception;

import com.microservices.products.dto.JsonApiError;
import com.microservices.products.dto.JsonApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("Validation error occurred", ex);
        
        List<JsonApiError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new JsonApiError(
                    "400",
                    "Validation Error", 
                    error.getField() + ": " + error.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(JsonApiResponse.error(errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<JsonApiResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        logger.error("Constraint violation error occurred", ex);
        
        List<JsonApiError> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new JsonApiError(
                    "400",
                    "Validation Error",
                    violation.getPropertyPath() + ": " + violation.getMessage()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.badRequest().body(JsonApiResponse.error(errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<JsonApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        logger.error("Invalid JSON format", ex);
        
        List<JsonApiError> errors = List.of(
            new JsonApiError("400", "Bad Request", "Invalid JSON format or malformed request body")
        );

        return ResponseEntity.badRequest().body(JsonApiResponse.error(errors));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<JsonApiResponse<Object>> handleNullPointerException(NullPointerException ex) {
        logger.error("Null pointer exception occurred", ex);
        
        List<JsonApiError> errors = List.of(
            new JsonApiError("400", "Bad Request", "Required field is missing or null")
        );

        return ResponseEntity.badRequest().body(JsonApiResponse.error(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonApiResponse<Object>> handleGenericException(Exception ex) {
        logger.error("Unexpected error occurred", ex);
        
        List<JsonApiError> errors = List.of(
            new JsonApiError("500", "Internal Server Error", "An unexpected error occurred")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JsonApiResponse.error(errors));
    }
}