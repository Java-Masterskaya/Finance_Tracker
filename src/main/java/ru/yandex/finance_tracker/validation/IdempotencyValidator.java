package ru.yandex.finance_tracker.validation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.yandex.finance_tracker.exception.IdempotencyKeyException;

import java.util.UUID;

@Component
public class IdempotencyValidator {

    public void validateKey(String ikey) {
        if(ikey == null || ikey.isBlank()) {
            throw new IdempotencyKeyException("Idempotency key is required",
                    HttpStatus.BAD_REQUEST);
        }

        try {
            UUID.fromString(ikey);
        } catch (IllegalArgumentException e) {
            throw new IdempotencyKeyException("Idempotency key must have UUID format",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
