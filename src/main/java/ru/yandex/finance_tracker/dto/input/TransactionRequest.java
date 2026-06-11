package ru.yandex.finance_tracker.dto.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Type;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotNull(message = "AccountId is required")
    @Positive
    private Long accountId;

    @NotNull(message = "Type is required")
    private Type type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Float amount;

    @NotNull(message = "Category is required")
    @NotBlank(message = "Category is required")
    private String category;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "date is required")
    @PastOrPresent
    private LocalDate date;

    private String description;
}
