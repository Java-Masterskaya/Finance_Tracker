package ru.yandex.finance_tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.exception.CurrencyMismatchException;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.storage.AccountRepository;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl {
    AccountRepository accountRepository;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        validateCurrency(request);  // ← добавить
        // остальная логика
    }

    // необходимо добавлять метод проверки соответствия валют транзакции и счёта
    // в методы создания и обновления транзакций
    private void validateCurrency(TransactionRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new NotFoundException(
                        "Account with ID = %d not found".formatted(request.getAccountId())
                ));

        if (!request.getCurrency().equals(account.getCurrency())) {
            throw new CurrencyMismatchException(request.getAccountId());
        }
    }
}
