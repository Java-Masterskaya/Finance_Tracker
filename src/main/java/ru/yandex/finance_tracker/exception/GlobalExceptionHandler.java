package ru.yandex.finance_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {
    private static final int HTTP_STATUS_NOT_FOUND = 404;
    private static final int HTTP_STATUS_CONFLICT = 409;
    private static final int HTTP_STATUS_BAD_REQUEST = 400;
    private static final int HTTP_STATUS_FORBIDDEN = 403;

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
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            final NotFoundException ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HTTP_STATUS_NOT_FOUND);
        body.put("error", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
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
    public ResponseEntity<Map<String, Object>> handleUserAlreadyExistsException(
            final UserAlreadyExistsException ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HTTP_STATUS_CONFLICT);
        body.put("error", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
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
    public ResponseEntity<Map<String, Object>> handleValidation(
            final MethodArgumentNotValidException ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HTTP_STATUS_BAD_REQUEST);
        body.put("error", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения нарушения прав доступа.
     * <p>
     * Перехватывает ситуации, когда авторизованный пользователь пытается получить
     * доступ к ресурсам, которые ему не принадлежат (например, чужой счет).
     * </p>
     *
     * @param ex исключение доступа (кастомное Acssecnot)
     * @return ResponseEntity с информацией о запрете доступа и статусом 403 (Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            final AccessDeniedException ex
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HTTP_STATUS_FORBIDDEN);
        body.put("error", ex.getMessage());

        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
}
