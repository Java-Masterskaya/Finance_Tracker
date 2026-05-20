package ru.yandex.finance_tracker.exception;

/**
 * Исключение, указывающее на то, что запрашиваемый ресурс не был найден в системе.
 * <p>
 * Используется в сценариях, когда выполняется попытка доступа к несуществующему
 * ресурсу, такому как пользователь, счёт, транзакция, запрос на участие и т.д.
 * </p>
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
