package ru.yandex.finance_tracker.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Currency;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfoDto {
    private Integer id;
    private String name;
    private Currency currency;
    private BigDecimal balance;
    private boolean overdraftAllowed;
}
