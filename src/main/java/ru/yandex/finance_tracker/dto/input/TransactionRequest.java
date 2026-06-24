package ru.yandex.finance_tracker.dto.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.validation.validator.ValidTransaction;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidTransaction
public class TransactionRequest {
    @NotNull(message = "AccountId is required")
    @Positive
    private Long accountId;

    @NotNull(message = "Type is required")
    private Type type;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Минимальная сумма транзакции — 0.01")
    @Digits(integer = 12, fraction = 2, message = "Разрешено максимум 2 знака после запятой")
    private BigDecimal amount;

    @NotNull
    private Currency currency;

    @NotNull
    @Positive
    private Long categoryId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @NotNull(message = "date is required")
    private Instant date;

    @Size(max = 300, message = "Transaction description must be shorter than 300 symbols")
    private String description;
}
