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
import ru.yandex.finance_tracker.validation.IdempotencyValidator;

import java.util.Optional;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;
    private final AuthenticationService authenticationService;
    private final IdempotencyService idempotencyService;
    private final IdempotencyValidator idempotencyValidator;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionInfoDto createTransaction(@RequestHeader("userId") Long userId,
                                                @Valid @RequestBody TransactionRequest request,
                                                @AuthenticationPrincipal UserDetails userDetails,
                                                @RequestHeader(value = "X-Idempotency-Key") String iKey) {

        log.debug("Валидация ключа идемпотентности ikey={}", iKey);
        idempotencyValidator.validateKey(iKey);

        Optional<TransactionInfoDto> cachedResponse = idempotencyService.getCachedResponse(iKey);
        if (cachedResponse.isPresent()) {
            log.info("Idempotent request: returning cached response for key={}", iKey);
            return cachedResponse.get();
        }

        if (!idempotencyService.tryLock(iKey)) {
            throw new IdempotencyKeyException("Duplicate request in progress", HttpStatus.CONFLICT);
        }

        try {
            log.info("POST /v1/transactions started for userid={}", userId);

            authenticationService.checkAuthority(userId, userDetails);
            TransactionInfoDto response = transactionService.createTransaction(userId, request);
            idempotencyService.cacheResponse(iKey, response);
            log.info("POST /v1/transactions finished: transactionId={}", response.transactionId());
            return response;
        } catch (Exception e) {
            idempotencyService.unlock(iKey);
            throw new ServerErrorException("Fail during transaction operation");
        }
    }
}
