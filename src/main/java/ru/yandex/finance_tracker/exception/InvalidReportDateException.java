package ru.yandex.finance_tracker.exception;

public class InvalidReportDateException extends RuntimeException {
    public InvalidReportDateException(int year, int month) {
        super("The requested date = %d.%d cannot be in the future".formatted(year, month));
    }
}
