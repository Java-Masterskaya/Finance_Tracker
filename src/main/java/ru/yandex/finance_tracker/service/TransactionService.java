package ru.yandex.finance_tracker.service;

import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;

public interface TransactionService {
    TransactionInfoDto createTransaction(Long userId, TransactionRequest request);
}
