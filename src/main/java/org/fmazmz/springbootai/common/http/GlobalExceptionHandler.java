package org.fmazmz.springbootai.common.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * API-wide JSON errors. {@link ApiErrorBody} is nested here so the error type always loads with this advice
 * (avoids split-classloader issues during dev reloads).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ApiErrorBody(int status, String message) {
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorBody> handleNoResource(NoResourceFoundException ex) {
        String path = ex.getResourcePath() != null ? ex.getResourcePath() : "unknown";
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorBody(HttpStatus.NOT_FOUND.value(), "No resource for path: " + path));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorBody> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiErrorBody(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "Method not allowed: " + ex.getMethod()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorBody> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorBody(HttpStatus.BAD_REQUEST.value(), "Malformed request body"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorBody> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorBody(
                        HttpStatus.BAD_REQUEST.value(),
                        "Missing required parameter: " + ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorBody> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorBody(
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid value for parameter: " + ex.getName()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorBody> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorBody(HttpStatus.FORBIDDEN.value(), "Access denied"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorBody> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorBody(HttpStatus.UNAUTHORIZED.value(), "Authentication required"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorBody> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(new ApiErrorBody(status.value(), message));
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ApiErrorBody> handleRestClient(RestClientResponseException ex) {
        HttpStatus status = HttpStatus.BAD_GATEWAY;
        String message = "Provider request failed with status " + ex.getStatusCode().value();
        if (ex.getStatusCode().value() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            status = HttpStatus.TOO_MANY_REQUESTS;
            message = "Provider rate limit reached. Please retry in a moment.";
        }
        return ResponseEntity.status(status).body(new ApiErrorBody(status.value(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorBody> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(new ApiErrorBody(HttpStatus.BAD_REQUEST.value(), message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorBody> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorBody(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorBody> handleGeneric(Exception ex) {
        log.warn("Unhandled exception: {}", ex.getClass().getName(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorBody(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unexpected server error"));
    }
}
