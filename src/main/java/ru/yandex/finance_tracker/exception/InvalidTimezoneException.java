package ru.yandex.finance_tracker.exception;

public class InvalidTimezoneException extends RuntimeException
{
    public InvalidTimezoneException(String timezone) {
        super("Invalid timezone: " + timezone);
    }
}
