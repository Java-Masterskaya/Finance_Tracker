package ru.yandex.finance_tracker.dto.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotNull
    @PositiveOrZero
    private Long accountId;
    @NotNull
    private Type type;
    @NotNull
    private Float amount;
    @NotNull
    private Currency currency;
    @NotNull
    private String category;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    private LocalDate date;
    private String description;
}
