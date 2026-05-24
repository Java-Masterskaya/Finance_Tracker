package ru.yandex.finance_tracker.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.IdempotencyKeyException;
import ru.yandex.finance_tracker.exception.ServerErrorException;
import ru.yandex.finance_tracker.idempotency.IdempotencyService;
import ru.yandex.finance_tracker.security.service.AuthenticationService;
import ru.yandex.finance_tracker.service.TransactionService;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;
    private final AuthenticationService authenticationService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionInfoDto createTransaction(@RequestHeader("userId") Long userId,
                                                @Valid @RequestBody TransactionRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails,
                                                @RequestHeader(value = "X-Idempotency-Key") String iKey) {
        if (iKey == null || iKey.isBlank()) {
            throw new IdempotencyKeyException("Idempotency key is missing", HttpStatus.BAD_REQUEST);
        }

        if (!idempotencyService.isUniqueRequest(iKey)) {
            throw new IdempotencyKeyException("Request duplication, rollback", HttpStatus.CONFLICT);
        }

        try {
            log.info("POST /v1/transactions started for userid={}", userId);

            authenticationService.checkAuthority(userId, userDetails);
            TransactionInfoDto response = transactionService.createTransaction(userId, request);
            log.info("POST /v1/transactions finished: transactionId={}", response.transactionId());
            return response;
        } catch (Exception e) {
            idempotencyService.removeKey(iKey);
            throw new ServerErrorException("Fail during transaction operation");
        }
    }
}
