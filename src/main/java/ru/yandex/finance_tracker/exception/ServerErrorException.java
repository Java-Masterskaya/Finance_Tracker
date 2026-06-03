package ru.yandex.finance_tracker.exception;

public class ServerErrorException extends RuntimeException {
    public ServerErrorException(String message) {
        super(message);
    }
}
