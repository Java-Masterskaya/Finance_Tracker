package ru.yandex.finance_tracker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.finance_tracker.baseclasses.PostgreSQLContainerForTests;
import ru.yandex.finance_tracker.dto.output.PagedTransactionResponse; // Подключаем новый тип ответа
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.*;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.CategoryRepository; // Добавили для создания категорий
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountServiceTests extends PostgreSQLContainerForTests {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountService accountService;

    @Test
    void shouldReturnTransactionsForAccount() {
        User user = userRepository.findById(1L).orElseThrow();

        Account account = accountRepository.save(Account.builder()
                .user(user)
                .name("shouldReturnTransactionsForAccount")
                .currency(Currency.RUB)
                .balance(BigDecimal.valueOf(100))
                .overdraftAllowed(false)
                .isDeleted(false)
                .updatedAt(Instant.now())
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("test-category")
                .isDeleted(false)
                .build());

        Transaction t1 = transactionRepository.save(Transaction.builder()
                .account(account)
                .type(Type.INCOME)
                .amount(BigDecimal.valueOf(100))
                .currency(Currency.RUB)
                .category(category)
                .date(Instant.now().minus(Duration.ofDays(1)))
                .description("t1")
                .accountBalance(BigDecimal.valueOf(100))
                .user(user)
                .isDeleted(false)
                .updatedAt(Instant.now())
                .build());

        Transaction t2 = transactionRepository.save(Transaction.builder()
                .account(account)
                .type(Type.INCOME)
                .amount(BigDecimal.valueOf(200))
                .currency(Currency.RUB)
                .category(category)
                .date(Instant.now())
                .description("t2")
                .accountBalance(BigDecimal.valueOf(300))
                .user(user)
                .isDeleted(false)
                .updatedAt(Instant.now())
                .build());

        PagedTransactionResponse result =
                accountService.getTransactionsByAccountId(
                        user.getId(),
                        account.getId(),
                        0,
                        10
                );

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountNotBelongsToUser() {
        User user = userRepository.findById(1L).orElseThrow();

        Account account = accountRepository.save(Account.builder()
                .user(user)
                .name("shouldThrowNotFoundExceptionWhenAccountNotBelongsToUser")
                .currency(Currency.RUB)
                .balance(BigDecimal.valueOf(100))
                .overdraftAllowed(false)
                .isDeleted(false)
                .updatedAt(Instant.now())
                .build());

        assertThrows(NotFoundException.class,
                () -> accountService.getTransactionsByAccountId(
                        999L,
                        account.getId(),
                        0,
                        10
                )
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactions() {
        User user = userRepository.findById(1L).orElseThrow();

        Account account = accountRepository.save(Account.builder()
                .user(user)
                .name("shouldReturnEmptyListWhenNoTransactions")
                .currency(Currency.RUB)
                .balance(BigDecimal.valueOf(100))
                .overdraftAllowed(false)
                .isDeleted(false)
                .updatedAt(Instant.now())
                .build());

        PagedTransactionResponse result =
                accountService.getTransactionsByAccountId(
                        user.getId(),
                        account.getId(),
                        0,
                        10
                );

        assertThat(result.getContent()).isEmpty();
    }
}