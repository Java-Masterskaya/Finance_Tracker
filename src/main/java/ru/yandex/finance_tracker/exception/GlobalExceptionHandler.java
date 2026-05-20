package ru.yandex.finance_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключения "Ресурс не найден".
     * <p>
     * Перехватывает ситуации, когда запрашиваемый ресурс (пользователь, счета,
     * транзакции и т.д.) не существует в системе.
     * </p>
     *
     * @param ex исключение "не найдено"
     * @return ResponseEntity с информацией об ошибке и статусом 404 (Not Found)
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundException(final NotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage());
    }

    /**
     * Обрабатывает исключения "Пользователь уже существует".
     * <p>
     * Перехватывает ситуации, когда при регистрации указан email,
     * который уже зарегистрирован в системе.
     * </p>
     *
     * @param ex исключение UserAlreadyExistsException
     * @return ResponseEntity с информацией об ошибке и статусом 409 (Conflict)
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExistsException(final UserAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    /**
     //     * Обрабатывает исключения нарушения прав доступа.
     //     * <p>
     //     * Перехватывает ситуации, когда авторизованный пользователь пытается получить
     //     * доступ к ресурсам, которые ему не принадлежат (например, чужой счет).
     //     * </p>
     //     *
     //     * @param ex исключение доступа Access Denied
     //     * @return ResponseEntity с информацией о запрете доступа и статусом 403 (Forbidden)
     //     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(final AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access Denied", ex.getMessage());
    }

    /**
     * Обрабатывает ошибки валидации входных данных.
     * <p>
     * Перехватывает исключения, возникающие при нарушении ограничений (constraints),
     * указанных в DTO (например, @NotNull, @NotBlank, @PositiveOrZero).
     * </p>
     *
     * @param ex исключение MethodArgumentNotValidException
     * @return ResponseEntity с подробностями валидации и статусом 400 (Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(final MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", details);
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String error, String message) {
        ApiError apiError = ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .build();
        return new ResponseEntity<>(apiError, status);
    }
}