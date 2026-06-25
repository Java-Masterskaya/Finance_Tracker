package ru.yandex.finance_tracker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.finance_tracker.baseclasses.PostgreSQLContainerForTests;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.*;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

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
    private AccountService accountService;

    @Test
    void shouldReturnTransactionsForAccount() {

        User user = userRepository.findById(1L)
                .orElseThrow();

        Account account = accountRepository.save(new Account(
                null,
                user,
                "shouldReturnTransactionsForAccount",
                Currency.RUB,
                BigDecimal.valueOf(100),
                false
        ));


        Transaction t1 = transactionRepository.save(new Transaction(
                null, account, Type.INCOME,
                BigDecimal.valueOf(100),
                Currency.RUB,
                "test",
                Instant.now().minus(Duration.ofDays(1)),
                "t1",
                user
        ));

        Transaction t2 = transactionRepository.save(new Transaction(
                null, account, Type.INCOME,
                BigDecimal.valueOf(200),
                Currency.RUB,
                "test2",
                Instant.now(),
                "t2",
                user
        ));

        List<TransactionInfoDto> result =
                accountService.getTransactionsByAccountId(
                        user.getId(),
                        account.getId(),
                        0,
                        10
                );

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountNotBelongsToUser() {
        User user = userRepository.findById(1L).orElseThrow();

        Account account = accountRepository.save(new Account(
                null,
                user,
                "shouldThrowNotFoundExceptionWhenAccountNotBelongsToUser",
                Currency.RUB,
                BigDecimal.valueOf(100),
                false
        ));

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

        Account account = accountRepository.save(new Account(
                null,
                user,
                "shouldReturnEmptyListWhenNoTransactions",
                Currency.RUB,
                BigDecimal.valueOf(100),
                false
        ));

        List<TransactionInfoDto> result =
                accountService.getTransactionsByAccountId(
                        user.getId(),
                        account.getId(),
                        0,
                        10
                );

        assertThat(result).isEmpty();
    }
}
