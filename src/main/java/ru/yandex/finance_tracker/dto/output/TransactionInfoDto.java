package ru.yandex.finance_tracker.dto.output;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.finance_tracker.model.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TransactionInfoDto(
        Long transactionId,
        Long accountId,
        Type type,
        BigDecimal amount,
        String category,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
        Instant date,
        String description,
        BigDecimal accountBalance
) {
}
