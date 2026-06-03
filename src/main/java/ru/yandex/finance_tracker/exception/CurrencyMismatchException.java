package ru.yandex.finance_tracker.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(Integer transactionId) {
        super("The transaction must have the same currency as the account with ID = %d".formatted(transactionId));
    }
}
