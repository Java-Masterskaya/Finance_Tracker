package ru.yandex.finance_tracker.dto.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Type;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @NotNull
    @PositiveOrZero
    Integer accountId;
    @NotNull
    Type type;
    @NotNull
    @PositiveOrZero
    Float amount;
    @NotNull
    String category;
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    @PastOrPresent
    LocalDate date;
    String description;
}
