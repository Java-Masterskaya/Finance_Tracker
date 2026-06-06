package ru.yandex.finance_tracker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
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
}
