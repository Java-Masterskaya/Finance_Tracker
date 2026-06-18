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

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

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

        // ищем счет, по id счета и владельца (счет должен принадлежать конкретному владельцу) с блокировкой и очередью
        Account account = accountRepository.findByIdAndUserIdWithLock(request.getAccountId(), userId)
                .orElseThrow(() -> new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE + request.getAccountId()));

        // проверка валюты
        validateCurrency(request, account);

        // считаем баланс
        BigDecimal newBalance = calculateNewBalance(account.getBalance(), request.getType(), request.getAmount());

        // Проверка ухода в минус если overdraft запрещен
        if (request.getType() == Type.EXPENSE
                && !account.isOverdraftAllowed()
                && newBalance.compareTo(BigDecimal.ZERO) < 0) {

            log.warn("Запрещено списание без овердрафта. Баланс={}, Попытка списания={}",
                    account.getBalance(), request.getAmount());
            throw new InsufficientBalanceException(
                    String.format("%s Баланс: %s, Попытка списания: %s",
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
    private BigDecimal calculateNewBalance(BigDecimal currentBalance, Type type, BigDecimal amount) {
        BigDecimal result = type == Type.INCOME ? currentBalance.add(amount) : currentBalance.subtract(amount);
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Проверяет, соответствует ли валюта транзакции валюте счёта.
     * <p>
     * Если валюты не совпадают, выбрасывается исключение.
     * Вызывается после того, как существование счёта подтверждено.
     * </p>
     *
     * @param request запрос на создание транзакции, содержащий валюту операции
     * @param account счёт, с которым связана транзакция, содержащий валюту счёта
     * @throws CurrencyMismatchException если валюта операции не совпадает с валютой счёта
     */
    private void validateCurrency(TransactionRequest request, Account account) {
        if (!request.getCurrency().equals(account.getCurrency())) {
            log.warn("Несовпадение валюты. Валюта счёта ID = {} - {}, валюта операции - {}",
                    account.getId(), account.getCurrency(), request.getCurrency());
            throw new CurrencyMismatchException(request.getAccountId());
        }
    }
}
