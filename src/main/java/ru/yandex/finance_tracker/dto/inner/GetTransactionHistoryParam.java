package ru.yandex.finance_tracker.dto.inner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.print.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTransactionHistoryParam {
    Integer accountId;
    Pageable pageable;
}
