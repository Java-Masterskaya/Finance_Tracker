package ru.yandex.finance_tracker.exception;

/**
 * Исключение, выбрасываемое при попытке регистрации пользователя с уже существующим email.
 * <p>
 * Используется в сервисе аутентификации для предотвращения дублирования пользователей.
 * </p>
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User with email %s already exists".formatted(email));
    }
}
