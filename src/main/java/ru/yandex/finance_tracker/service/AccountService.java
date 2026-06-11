package ru.yandex.finance_tracker.service;

import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;

import java.util.List;

public interface AccountService {
    List<AccountInfoDto> getAccountsByUserId(Long userId);

    AccountInfoDto createAccount(Long userId, AccountCreateRequest request);

    List<TransactionInfoDto> getTransactionsByAccountId(Long userId, Long accountId,  int page, int size);
}
