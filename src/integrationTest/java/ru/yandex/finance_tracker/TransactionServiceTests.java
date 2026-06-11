package ru.yandex.finance_tracker;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.model.*;
import ru.yandex.finance_tracker.service.TransactionService;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransactionServiceTests extends BaseIntegrationTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    @Order(1)
    void shouldCreateIncomeTransaction() {

        User user = new User(
                null,
                "test@mail.com",
                "TEST1234567890",
                "TestUser",
                UserRole.ROLE_USER,
                new ArrayList<>(),
                new ArrayList<>()
        );
        user = userRepository.save(user);

        Account account = new Account(
                null,
                user,
                "Main account",
                Currency.RUB,
                100.0f
        );
        account = accountRepository.save(account);
        TransactionRequest request = new TransactionRequest(
                account.getId(),
                Type.INCOME,
                50F,
                "Salary",
                LocalDate.now(),
                "June salary"
        );

        TransactionInfoDto result =
                transactionService.createTransaction(
                        user.getId(),
                        request
                );

        Account updated =
                accountRepository.findById(account.getId())
                        .orElseThrow();

        assertNotNull(result);
        assertEquals(150F, updated.getBalance());

        assertEquals(
                1,
                transactionRepository.count()
        );
    }

    @Test
    @Order(2)
    void shouldCreateExpenseTransaction() {

        User user = new User(
                null,
                "test1@mail.com",
                "TEST11234567890",
                "TestUser1",
                UserRole.ROLE_USER,
                new ArrayList<>(),
                new ArrayList<>()
        );
        user = userRepository.save(user);

        Account account = new Account(
                null,
                user,
                "Main account",
                Currency.RUB,
                100.0f
        );
        account = accountRepository.save(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(),
                Type.EXPENSE,
                40F,
                "Food",
                LocalDate.now(),
                "Lunch"
        );

        transactionService.createTransaction(
                user.getId(),
                request
        );

        Account updated =
                accountRepository.findById(account.getId())
                        .orElseThrow();

        assertEquals(60F, updated.getBalance());

        assertEquals(
                2,
                transactionRepository.count()
        );
    }
}
