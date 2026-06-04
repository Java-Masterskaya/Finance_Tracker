package ru.yandex.finance_tracker.exception;

/**
 * Исключение, выбрасываемое при несовпадении валюты транзакции и валюты счёта.
 * <p>
 * Возникает, когда в запросе на создание транзакции указана валюта,
 * отличная от валюты счёта, к которому привязывается транзакция.
 * </p>
 */

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(Long transactionId) {
        super("The transaction must have the same currency as the account with ID = %d".formatted(transactionId));
    }
}
