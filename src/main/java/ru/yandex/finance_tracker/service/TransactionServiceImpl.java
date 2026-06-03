package ru.yandex.finance_tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.exception.CurrencyMismatchException;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.InsufficientBalanceException;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.mapper.TransactionMapper;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.Transaction;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService{

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper mapper;

    private static final String ACCOUNT_NOT_FOUND_MESSAGE = "Счет не найден с id: ";
    private static final String INSUFFICIENT_BALANCE_MESSAGE = "Недостаточно средств на счете";

    @Transactional
    @Override
    public TransactionInfoDto createTransaction(Long userId, TransactionRequest request) {
        log.info("Создание транзакции для пользователя ID={}, счёт ID={}, тип={}",
                userId, request.getAccountId(), request.getType());

        // ищем счет, по id счета и владельца (счет должен принадлежать конкретному владельцу)
        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
                .orElseThrow(()-> {
                    log.warn("Счёт {} не найден или не принадлежит пользователю {}", request.getAccountId(), userId);
                    return new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE + request.getAccountId());
                });

        // считаем баланс
        float newBalance = calculateNewBalance(account.getBalance(), request.getType(), request.getAmount());

        // Проверка только для расходов и если баланс становится отрицательным
        if(request.getType() == Type.EXPENSE && newBalance < 0) {
            log.warn("Недостаточно средств. Баланс={}, Попытка списания={}",
                    account.getBalance(), request.getAmount());
            throw new InsufficientBalanceException(
                    String.format("%s Баланс: %.2f, Попытка списания: %.2f",
                            INSUFFICIENT_BALANCE_MESSAGE, account.getBalance(), request.getAmount()));
        }

        account.setBalance(newBalance);

        Transaction transaction = mapper.toEntity(request);
        transaction.setAccount(account);
        transaction.setUser(account.getUser());

        accountRepository.save(account);
        Transaction saved = transactionRepository.save(transaction);

        log.info("Создание транзакции для пользователя завершено: ID={}, новый баланс счета={}",
                saved.getTransactionId(), newBalance);

        return mapper.toResponse(saved);
    }

    /** Считаем баланс, в зависимости от типа операции вычитаем или складываем */
    private float calculateNewBalance(float currentBalance, Type type, float amount) {
        return type == Type.INCOME ? currentBalance + amount : currentBalance - amount;
    }

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
