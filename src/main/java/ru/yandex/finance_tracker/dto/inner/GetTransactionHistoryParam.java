package ru.yandex.finance_tracker.dto.inner;

import lombok.Data;

import java.awt.print.Pageable;

@Data
public class GetTransactionHistoryParam {
    Integer accountId;
    Pageable pageable;
}
