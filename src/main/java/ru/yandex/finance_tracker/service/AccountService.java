package ru.yandex.finance_tracker.service;

import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.input.AccountUpdateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
import ru.yandex.finance_tracker.dto.output.PagedTransactionResponse;

import java.util.List;

public interface AccountService {
    List<AccountInfoDto> getAccountsByUserId(Long userId);

    AccountInfoDto createAccount(Long userId, AccountCreateRequest request);

    PagedTransactionResponse getTransactionsByAccountId(Long userId, Long accountId, int page, int size);

    AccountInfoDto updateAccount(Long userId, Long accountId, AccountUpdateRequest request);

    void deleteAccount(Long userId, Long accountId);
}
