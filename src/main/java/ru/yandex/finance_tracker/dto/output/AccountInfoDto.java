package ru.yandex.finance_tracker.dto.output;

import lombok.Data;
import ru.yandex.finance_tracker.model.Currency;

@Data
public class AccountInfoDto {
    Long id;
    String name;
    Currency currency;
    Float balance;
}
