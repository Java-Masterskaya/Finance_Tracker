package ru.yandex.finance_tracker.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


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
     * Обрабатывает исключение, возникающее при несовпадении валют.
     * <p>
     * Исключение выбрасывается, когда валюта операции не соответствует валюте счёта.
     * Возвращает статус 400 (Bad Request) с информацией об ошибке.
     * </p>
     *
     * @param ex исключение, содержащее сообщение о несовпадении валют
     * @return ResponseEntity с подробностями валидации и статусом 400 (Bad Request)
     */
    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ApiError> handleValidationCurrency(final CurrencyMismatchException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", ex.getMessage());
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

    /**
     * Обрабатывает ошибки валидации OpenAPI.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                ex.getMessage());
    }

    /**
     * Обрабатывает исключения нахождения ключа идемпотентности в кеше Redis
     * <p>
     * При попытке отправить запрос с одним и тем же ключом идемпотентности пользователь получит
     * сообщение о том что его транзакция уже в работе и не получит/потеряет свои деньги дважды или более раз
     * </p>
     *
     * @param ex исключение IdempotencyKeyException
     * @return ResponseEntity с информацией об исключении и статусом 409 (Conflict)
     */
    @ExceptionHandler(IdempotencyKeyException.class)
    public ResponseEntity<ApiError> handleIdempotencyException(final IdempotencyKeyException ex) {
        return buildResponse(ex.getStatus(), "Idempotency key already in use", ex.getMessage());
    }

    /**
     * Обрабатывает исключения возникшие во время выполнения программы
     * <p>
     * Представляет из себя handler для отлова обобщенных ошибок возникших в процессе работы,
     * на момент создания реализован только в контроллере транзакций
     * </p>
     *
     * @param ex исключение ServerErrorException
     * @return ResponseEntity с информацией, приложенной в message ошибки и статусом 500 (Internal Server Error)
     */

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<ApiError> handleServerErrorException(final ServerErrorException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Fail during program run", ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", ex.getMessage());
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(), ex.getRequiredType().getSimpleName());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
    }

    /**
     * Обрабатывает исключение нехватки средств на счёте.
     * <p>
     * Выбрасывается, когда сумма списания превышает текущий баланс аккаунта,
     * и овердрафт для данного счёта запрещён.
     * </p>
     *
     * @param ex исключение InsufficientBalanceException
     * @return ResponseEntity с информацией об ошибке и статусом 400 (Bad Request)
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiError> handleInsufficientBalance(final InsufficientBalanceException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Insufficient Funds", ex.getMessage());
    }

    /**
     * Обрабатывает исключение, возникающее при некорректной дате.
     * <p>
     * Исключение выбрасывается, когда дата в будущем.
     * Возвращает статус 400 (Bad Request) с информацией об ошибке.
     * </p>
     *
     * @param ex исключение, содержащее сообщение о несоответствии даты
     * @return ResponseEntity с подробностями валидации и статусом 400 (Bad Request)
     */
    @ExceptionHandler(InvalidReportDateException.class)
    public ResponseEntity<ApiError> handleInvalidReportDate(final InvalidReportDateException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Failed", ex.getMessage());
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
