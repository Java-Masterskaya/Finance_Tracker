package ru.yandex.finance_tracker.service;

import org.springframework.stereotype.Service;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;

@Service
public class TransactionServiceImpl implements TransactionService{

    @Override
    public TransactionInfoDto createTransaction(Long userId, TransactionRequest request) {
        return null;
    }
}
