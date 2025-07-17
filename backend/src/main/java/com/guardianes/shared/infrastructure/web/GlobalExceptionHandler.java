package com.guardianes.shared.infrastructure.web;

import com.guardianes.walking.domain.GuardianNotFoundException;
import com.guardianes.walking.domain.InsufficientEnergyException;
import com.guardianes.walking.domain.RateLimitExceededException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GuardianNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGuardianNotFound(GuardianNotFoundException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        logger.warn("Guardian not found [{}]: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Guardian Not Found")
                .message(ex.getMessage())
                .path(getPath(request))
                .correlationId(correlationId)
                .build();
                
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(InsufficientEnergyException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientEnergy(InsufficientEnergyException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        logger.warn("Insufficient energy [{}]: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Insufficient Energy")
                .message(ex.getMessage())
                .path(getPath(request))
                .correlationId(correlationId)
                .build();
                
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        logger.warn("Rate limit exceeded [{}]: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Rate Limit Exceeded")
                .message(ex.getMessage())
                .path(getPath(request))
                .correlationId(correlationId)
                .build();
                
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("X-RateLimit-Remaining", "0")
                .header("X-Correlation-ID", correlationId)
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        logger.warn("Validation failed [{}]: {} field errors", correlationId, ex.getFieldErrorCount());
        
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .path(getPath(request))
                .correlationId(correlationId)
                .fieldErrors(fieldErrors)
                .build();
                
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        logger.warn("Malformed JSON request [{}]: {}", correlationId, ex.getMessage());
        
        String message = "Malformed JSON request";
        if (ex.getMessage().contains("Cannot deserialize")) {
            if (ex.getMessage().contains("EnergySpendingSource")) {
                message = "Invalid energy spending source";
            } else {
                message = "Invalid request format";
            }
        }
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Bad Request")
                .message(message)
                .path(getPath(request))
                .correlationId(correlationId)
                .build();
                
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        logger.warn("Invalid argument [{}]: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Request")
                .message(ex.getMessage())
                .path(getPath(request))
                .correlationId(correlationId)
                .build();
                
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        logger.warn("No handler found [{}]: {}", correlationId, ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message("The requested resource was not found")
                .path(getPath(request))
                .correlationId(correlationId)
                .build();
                
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String correlationId = generateCorrelationId();
        logger.error("Unexpected error [{}]: {}", correlationId, ex.getMessage(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("An unexpected error occurred")
                .path(getPath(request))
                .correlationId(correlationId)
                .build();
                
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    // Error Response DTOs
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private String correlationId;

        public ErrorResponse() {}

        public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, String correlationId) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.correlationId = correlationId;
        }

        public static ErrorResponseBuilder builder() {
            return new ErrorResponseBuilder();
        }

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

        public static class ErrorResponseBuilder {
            private LocalDateTime timestamp;
            private int status;
            private String error;
            private String message;
            private String path;
            private String correlationId;

            public ErrorResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public ErrorResponseBuilder status(int status) { this.status = status; return this; }
            public ErrorResponseBuilder error(String error) { this.error = error; return this; }
            public ErrorResponseBuilder message(String message) { this.message = message; return this; }
            public ErrorResponseBuilder path(String path) { this.path = path; return this; }
            public ErrorResponseBuilder correlationId(String correlationId) { this.correlationId = correlationId; return this; }

            public ErrorResponse build() {
                return new ErrorResponse(timestamp, status, error, message, path, correlationId);
            }
        }
    }

    public static class ValidationErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private String correlationId;
        private List<FieldError> fieldErrors;

        public ValidationErrorResponse() {}

        public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, String correlationId, List<FieldError> fieldErrors) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.correlationId = correlationId;
            this.fieldErrors = fieldErrors;
        }

        public static ValidationErrorResponseBuilder builder() {
            return new ValidationErrorResponseBuilder();
        }

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        public List<FieldError> getFieldErrors() { return fieldErrors; }
        public void setFieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; }

        public static class ValidationErrorResponseBuilder {
            private LocalDateTime timestamp;
            private int status;
            private String error;
            private String message;
            private String path;
            private String correlationId;
            private List<FieldError> fieldErrors;

            public ValidationErrorResponseBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
            public ValidationErrorResponseBuilder status(int status) { this.status = status; return this; }
            public ValidationErrorResponseBuilder error(String error) { this.error = error; return this; }
            public ValidationErrorResponseBuilder message(String message) { this.message = message; return this; }
            public ValidationErrorResponseBuilder path(String path) { this.path = path; return this; }
            public ValidationErrorResponseBuilder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
            public ValidationErrorResponseBuilder fieldErrors(List<FieldError> fieldErrors) { this.fieldErrors = fieldErrors; return this; }

            public ValidationErrorResponse build() {
                return new ValidationErrorResponse(timestamp, status, error, message, path, correlationId, fieldErrors);
            }
        }
    }

    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;

        public FieldError() {}

        public FieldError(String field, String message, Object rejectedValue) {
            this.field = field;
            this.message = message;
            this.rejectedValue = rejectedValue;
        }

        public static FieldErrorBuilder builder() {
            return new FieldErrorBuilder();
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getRejectedValue() { return rejectedValue; }
        public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }

        public static class FieldErrorBuilder {
            private String field;
            private String message;
            private Object rejectedValue;

            public FieldErrorBuilder field(String field) { this.field = field; return this; }
            public FieldErrorBuilder message(String message) { this.message = message; return this; }
            public FieldErrorBuilder rejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; return this; }

            public FieldError build() {
                return new FieldError(field, message, rejectedValue);
            }
        }
    }
}