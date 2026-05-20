package ru.yandex.finance_tracker.dto.output;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.finance_tracker.model.Currency;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfoDto {
    private Integer id;
    private String name;
    private Currency currency;
    private Float balance;
}
