package ru.yandex.finance_tracker.dto.input;

import jakarta.validation.constraints.*;
import ru.yandex.finance_tracker.model.Type;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionUpdateRequest(
        @DecimalMin(value = "0.01", message = "Минимальная сумма транзакции — 0.01")
        @Digits(integer = 12, fraction = 2, message = "Разрешено максимум 2 знака после запятой")
        BigDecimal amount,

        Type type,

        @Positive(message = "CategoryId must be positive")
        Long categoryId,

        Instant date,

        @Size(max = 300, message = "Transaction description must be shorter than 300 symbols")
        String description
) {}