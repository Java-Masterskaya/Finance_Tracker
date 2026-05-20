package ru.yandex.finance_tracker.service;

import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;

import java.util.List;

public interface AccountService {
    List<AccountInfoDto> getAccountsByUserId(Long userId);

    AccountInfoDto createAccount(Long userId, AccountCreateRequest request);
}
