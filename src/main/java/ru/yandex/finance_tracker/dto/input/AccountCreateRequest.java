package ru.yandex.finance_tracker.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Currency;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {
    @NotBlank
    @Size(max = 100, message = "Account name must be shorter than 100 symbols")
    private String name;
    @NotNull
    private Currency currency;
    @NotNull
    @PositiveOrZero
    private BigDecimal initialBalance;
}
