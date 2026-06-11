package ru.yandex.finance_tracker.dto.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.validation.validator.ValidTransaction;

import java.time.LocalDate;

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
    private Float amount;

    @NotNull
    private Currency currency;

    @Size(max = 50, message = "Category length must be shorter than 50 symbols")
    @NotBlank(message = "Category is required")
    private String category;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "date is required")
    private LocalDate date;

    @Size(max = 300, message = "Transaction description must be shorter than 300 symbols")
    private String description;
}
