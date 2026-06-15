package ru.yandex.finance_tracker.dto.output;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yandex.finance_tracker.model.Type;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionInfoDto(
        Long transactionId,
        Long accountId,
        Type type,
        BigDecimal amount,
        String category,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        String description,
        BigDecimal accountBalance
) {
}
