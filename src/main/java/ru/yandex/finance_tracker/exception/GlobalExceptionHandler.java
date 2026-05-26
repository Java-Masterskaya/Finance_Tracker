package ru.yandex.finance_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final int HTTP_STATUS_CONFLICT = 409;
    private static final int HTTP_STATUS_FORBIDDEN = 403;
    private static final int HTTP_STATUS_BAD_REQUEST = 400;
    private static final int HTTP_STATUS_INTERNAL_ERROR = 500;

    /**
     * Обрабатывает исключения "Ресурс не найден".
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            NotFoundException ex, WebRequest request) {
        return buildResponse(HTTP_STATUS_NOT_FOUND, "Resource Not Found", 
                           ex.getMessage(), request);
    }

    /**
     * Обрабатывает исключения "Пользователь уже существует".
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {
        return buildResponse(HTTP_STATUS_CONFLICT, "Conflict", 
                           ex.getMessage(), request);
    }

    /**
     * Обрабатывает исключения нарушения прав доступа.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        return buildResponse(HTTP_STATUS_FORBIDDEN, "Access Denied", 
                           ex.getMessage(), request);
    }

    /**
     * Обрабатывает ошибки валидации входных данных.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HTTP_STATUS_BAD_REQUEST, "Validation Failed", 
                           details, request);
    }

    /**
     * Обрабатывает ошибки валидации OpenAPI.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            IllegalArgumentException ex, WebRequest request) {
        return buildResponse(HTTP_STATUS_BAD_REQUEST, "Bad Request", 
                           ex.getMessage(), request);
    }

    /**
     * Обрабатывает все остальные исключения.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(
            Exception ex, WebRequest request) {
        return buildResponse(HTTP_STATUS_INTERNAL_ERROR, "Internal Server Error", 
                           ex.getMessage(), request);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            int status, String error, String message, WebRequest request) {
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.valueOf(status));
    }
}
