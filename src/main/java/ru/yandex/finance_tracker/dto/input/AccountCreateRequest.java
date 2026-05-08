package ru.yandex.finance_tracker.dto.input;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import ru.yandex.finance_tracker.model.Currency;

@Data
public class AccountCreateRequest {
    @NotNull
    String name;
    @NotNull
    Currency currency;
    @NotNull
    @PositiveOrZero
    Float initialBalance;
}
