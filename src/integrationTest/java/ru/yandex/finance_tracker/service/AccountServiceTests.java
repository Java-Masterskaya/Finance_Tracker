package ru.yandex.finance_tracker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.finance_tracker.BaseIntegrationTest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.AccessDeniedException;
import ru.yandex.finance_tracker.model.*;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccountServiceTests extends BaseIntegrationTest {
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
                LocalDate.now().minusDays(1),
                "t1",
                user
        ));

        Transaction t2 = transactionRepository.save(new Transaction(
                null, account, Type.INCOME,
                BigDecimal.valueOf(200),
                Currency.RUB,
                "test2",
                LocalDate.now(),
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
    void shouldThrowAccessDeniedWhenAccountNotBelongsToUser() {

        User user = userRepository.findById(1L).orElseThrow();

        Account account = accountRepository.save(new Account(
                null,
                user,
                "shouldThrowAccessDeniedWhenAccountNotBelongsToUser",
                Currency.RUB,
                BigDecimal.valueOf(100),
                false
        ));

        assertThrows(AccessDeniedException.class,
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
