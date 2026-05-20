package ru.yandex.finance_tracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
import ru.yandex.finance_tracker.security.service.AuthenticationService;
import ru.yandex.finance_tracker.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {
    private final AccountService accountService;
    private final AuthenticationService authenticationService;


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<AccountInfoDto> getUserAccounts(
            @RequestHeader("userId") @NotNull Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        authenticationService.checkAuthority(userId, userDetails);

        return accountService.getAccountsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountInfoDto createAccount(
            @RequestHeader("userId") @NotNull Long userId,
            @Valid @RequestBody AccountCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        authenticationService.checkAuthority(userId, userDetails);

        return accountService.createAccount(userId, request);
    }
}
