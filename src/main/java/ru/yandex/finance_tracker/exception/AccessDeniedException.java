package ru.yandex.finance_tracker.exception;

/**
 * Исключение, выбрасываемое при попытке доступа к ресурсам, на которые у пользователя нет прав.
 * <p>
 * Используется для предотвращения уязвимостей типа IDOR (Insecure Direct Object Reference),
 * например, когда один пользователь пытается просмотреть или изменить счета другого пользователя.
 * </p>
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
