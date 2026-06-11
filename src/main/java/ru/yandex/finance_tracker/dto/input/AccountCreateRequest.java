package ru.yandex.finance_tracker.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {
    @NotBlank
    @Size(max = 100, message = "Длина названия счёта не должна превышать 100 символов")
    private String name;
    @NotNull
    private Currency currency;
    @NotNull
    @PositiveOrZero
    private Float initialBalance;
}
