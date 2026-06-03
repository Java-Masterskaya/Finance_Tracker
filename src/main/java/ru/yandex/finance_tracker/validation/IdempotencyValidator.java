package ru.yandex.finance_tracker.validation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.yandex.finance_tracker.exception.IdempotencyKeyException;

@Component
public class IdempotencyValidator {

    public void validateKey(String ikey) {
        if(ikey == null || ikey.isBlank()) {
            throw new IdempotencyKeyException("Idempotency key is required",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
