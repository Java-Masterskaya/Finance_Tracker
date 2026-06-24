package ru.yandex.finance_tracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.input.AccountUpdateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
import ru.yandex.finance_tracker.dto.output.PagedTransactionResponse;
import ru.yandex.finance_tracker.security.dto.AuthInfo;
import ru.yandex.finance_tracker.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {
    private final AccountService accountService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccountInfoDto> getUserAccounts(@AuthenticationPrincipal AuthInfo authInfo) {
        return accountService.getAccountsByUserId(authInfo.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountInfoDto createAccount(@Valid @RequestBody AccountCreateRequest request,
                                        @AuthenticationPrincipal AuthInfo authInfo) {
        return accountService.createAccount(authInfo.getId(), request);
    }

    @GetMapping("/{accountId}/transaction")
    @ResponseStatus(HttpStatus.OK)
    public PagedTransactionResponse getTransactions(
            @PathVariable(name = "accountId") @Positive Long accountId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive int size,
            @AuthenticationPrincipal AuthInfo authInfo) {
        return accountService.getTransactionsByAccountId(authInfo.getId(), accountId, page, size);
    }

    @PutMapping("/{accountId}")
    @ResponseStatus(HttpStatus.OK)
    public AccountInfoDto updateAccount(@PathVariable(name = "accountId") @Positive Long accountId,
                                        @Valid @RequestBody AccountUpdateRequest request,
                                        @AuthenticationPrincipal AuthInfo authInfo) {
        return accountService.updateAccount(authInfo.getId(), accountId, request);
    }


    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable(name = "accountId") @Positive Long accountId,
                              @AuthenticationPrincipal AuthInfo authInfo) {
        accountService.deleteAccount(authInfo.getId(), accountId);
    }
}