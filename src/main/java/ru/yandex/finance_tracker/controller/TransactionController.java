package ru.yandex.finance_tracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.IdempotencyKeyException;
import ru.yandex.finance_tracker.exception.ServerErrorException;
import ru.yandex.finance_tracker.idempotency.IdempotencyService;
import ru.yandex.finance_tracker.security.dto.AuthInfo;
import ru.yandex.finance_tracker.service.TransactionService;

import java.util.Optional;

@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TransactionController {
    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionInfoDto createTransaction(@Valid @RequestBody TransactionRequest request,
                                                @AuthenticationPrincipal AuthInfo authInfo,

                                                @NotBlank(message = "Idempotency key is required")
                                                @UUID(message = "Idempotency key must have UUID format")
                                                @RequestHeader(value = "X-Idempotency-Key") String iKey) {

        Optional<TransactionInfoDto> cachedResponse = idempotencyService.getCachedResponse(iKey);
        if (cachedResponse.isPresent()) {
            log.info("Idempotent request: returning cached response for key={}", iKey);
            return cachedResponse.get();
        }

        if (!idempotencyService.tryLock(iKey)) {
            throw new IdempotencyKeyException("Duplicate request in progress", HttpStatus.CONFLICT);
        }

        try {
            log.info("POST /v1/transactions started for userid={}", authInfo.getId());

            TransactionInfoDto response = transactionService.createTransaction(authInfo.getId(), request);
            idempotencyService.cacheResponse(iKey, response);
            log.info("POST /v1/transactions finished: transactionId={}", response.transactionId());
            return response;
        } catch (Exception e) {
            idempotencyService.unlock(iKey);
            throw new ServerErrorException("Fail during transaction operation");
        }
    }
}
