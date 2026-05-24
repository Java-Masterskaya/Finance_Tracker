package ru.yandex.finance_tracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IdempotencyKeyException extends RuntimeException {
    public HttpStatus status;

    public IdempotencyKeyException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
