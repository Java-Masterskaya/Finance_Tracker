package ru.yandex.finance_tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.input.TransactionUpdateRequest;
import ru.yandex.finance_tracker.exception.InsufficientBalanceException;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.*;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.CategoryRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class TransactionServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1");

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CategoryRepository categoryRepository;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Test user with ID 1L not found"));

        testCategory = categoryRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Test category with ID 1L not found"));
    }

    @Test
    public void shouldPreventDoubleSpendingUnderHighConcurrency() throws InterruptedException {
        Account account = new Account(null, testUser, "test", Currency.RUB, new BigDecimal("100.00"), false, false, Instant.now());
        account = accountRepository.saveAndFlush(account);
        Long accountId = account.getId();

        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Throwable> unexpectedErrors = Collections.synchronizedList(new ArrayList<>());

        TransactionRequest request = new TransactionRequest(accountId,
                Type.EXPENSE,
                new BigDecimal("30.00"),
                Currency.RUB,
                testCategory.getId(),
                Instant.now(),
                "test");

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    transactionService.createTransaction(testUser.getId(), request);
                    successCount.incrementAndGet();
                } catch (InsufficientBalanceException e) {
                    failureCount.incrementAndGet();
                } catch (Throwable e) {
                    unexpectedErrors.add(e);
                }
            });
        }

        latch.countDown();
        executor.shutdown();

        boolean finishedCleanly = executor.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(unexpectedErrors).isEmpty();
        assertThat(finishedCleanly).isTrue();

        Account updatedAccount = accountRepository.findById(accountId).orElseThrow();

        assertThat(successCount.get()).isEqualTo(3);
        assertThat(failureCount.get()).isEqualTo(2);
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("10.00");
    }

    @Test
    public void shouldThrowException_WhenOverdraftDisabledAndBalanceGoesNegative() {
        Account account = new Account(null, testUser, "overdraft-disabled-test", Currency.RUB, new BigDecimal("100.00"), false, false, Instant.now());
        account = accountRepository.saveAndFlush(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(), Type.EXPENSE, new BigDecimal("150.00"), Currency.RUB, testCategory.getId(), Instant.now(), "check"
        );

        assertThrows(InsufficientBalanceException.class, () ->
                transactionService.createTransaction(testUser.getId(), request)
        );

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    public void shouldAllowNegativeBalance_WhenOverdraftEnabled() {
        Account account = new Account(null, testUser, "overdraft-enabled-test", Currency.RUB, new BigDecimal("100.00"), true, false, Instant.now());
        account = accountRepository.saveAndFlush(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(), Type.EXPENSE, new BigDecimal("150.00"), Currency.RUB, testCategory.getId(), Instant.now(), "check"
        );

        transactionService.createTransaction(testUser.getId(), request);

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("-50.00");
    }

    @Test
    public void shouldReturnCorrectPaginationMetadataAndSorting() {
        Account account = new Account(null, testUser, "pagination-test", Currency.RUB, new BigDecimal("1000.00"), false, false, Instant.now());
        account = accountRepository.saveAndFlush(account);
        Long accountId = account.getId();

        TransactionRequest tx1 = new TransactionRequest(accountId, Type.INCOME, new BigDecimal("100.00"), Currency.RUB, testCategory.getId(), Instant.now().minusSeconds(60), "First");
        TransactionRequest tx2 = new TransactionRequest(accountId, Type.INCOME, new BigDecimal("200.00"), Currency.RUB, testCategory.getId(), Instant.now().minusSeconds(30), "Second");
        TransactionRequest tx3 = new TransactionRequest(accountId, Type.INCOME, new BigDecimal("300.00"), Currency.RUB, testCategory.getId(), Instant.now(), "Third");

        transactionService.createTransaction(testUser.getId(), tx1);
        transactionService.createTransaction(testUser.getId(), tx2);
        transactionService.createTransaction(testUser.getId(), tx3);

        int page = 0;
        int size = 2;

        var response = accountService.getTransactionsByAccountId(testUser.getId(), accountId, page, size);

        assertThat(response).isNotNull();
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(3L);
        assertThat(response.getTotalPages()).isEqualTo(2);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).description()).isEqualTo("Third");
        assertThat(response.getContent().get(1).description()).isEqualTo("Second");
    }

    @Test
    public void shouldCorrectlyRecalculateBalance_WhenExpenseTransactionIsDeleted() {
        Account account = new Account(null, testUser, "recalc-test", Currency.RUB, new BigDecimal("100.00"), false, false, Instant.now());
        account = accountRepository.saveAndFlush(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(), Type.EXPENSE, new BigDecimal("30.00"), Currency.RUB, testCategory.getId(), Instant.now(), "Buy something"
        );
        var createdTx = transactionService.createTransaction(testUser.getId(), request);

        Account accountAfterTx = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(accountAfterTx.getBalance()).isEqualByComparingTo("70.00");

        transactionService.deleteTransaction(testUser.getId(), createdTx.transactionId());

        Account accountAfterDelete = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(accountAfterDelete.getBalance()).isEqualByComparingTo("100.00");

        var history = accountService.getTransactionsByAccountId(testUser.getId(), account.getId(), 0, 10);
        assertThat(history.getContent()).isEmpty();
    }

    @Test
    public void shouldUpdateOnlyDescription_WithoutRecalculatingBalance() {
        Account account = new Account(null, testUser, "update-desc-test", Currency.RUB, new BigDecimal("500.00"), false, false, Instant.now());
        account = accountRepository.saveAndFlush(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(), Type.EXPENSE, new BigDecimal("50.00"), Currency.RUB, testCategory.getId(), Instant.now(), "Old Description"
        );
        var createdTx = transactionService.createTransaction(testUser.getId(), request);

        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest(null, null, null, null, "New Super Description");

        var updatedTx = transactionService.updateTransaction(testUser.getId(), createdTx.transactionId(), updateRequest);

        assertThat(updatedTx.description()).isEqualTo("New Super Description");
        assertThat(updatedTx.amount()).isEqualByComparingTo("50.00");

        Account accountAfterUpdate = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(accountAfterUpdate.getBalance()).isEqualByComparingTo("450.00");
    }

    @Test
    public void shouldRecalculateBalance_WhenTransactionAmountIsUpdated() {
        Account account = new Account(null, testUser, "update-amount-test", Currency.RUB, new BigDecimal("200.00"), false, false, Instant.now());
        account = accountRepository.saveAndFlush(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(), Type.EXPENSE, new BigDecimal("40.00"), Currency.RUB, testCategory.getId(), Instant.now(), "Dinner"
        );
        var createdTx = transactionService.createTransaction(testUser.getId(), request);

        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest(new BigDecimal("100.00"), Type.EXPENSE, null, null, "Dinner with wine");

        transactionService.updateTransaction(testUser.getId(), createdTx.transactionId(), updateRequest);

        Account accountAfterUpdate = accountRepository.findById(account.getId()).orElseThrow();

        assertThat(accountAfterUpdate.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    public void shouldSoftDeleteTransactions_WhenAccountIsDeleted() {
        Account account = new Account(null, testUser, "cascade-test", Currency.RUB, new BigDecimal("100.00"), false, false, Instant.now());
        account = accountRepository.saveAndFlush(account);

        final Long accountId = account.getId();

        TransactionRequest request = new TransactionRequest(
                accountId, Type.INCOME, new BigDecimal("50.00"), Currency.RUB, testCategory.getId(), Instant.now(), "Salary"
        );
        transactionService.createTransaction(testUser.getId(), request);

        var historyBefore = accountService.getTransactionsByAccountId(testUser.getId(), accountId, 0, 10);
        assertThat(historyBefore.getContent()).hasSize(1);

        accountService.deleteAccount(testUser.getId(), accountId);

        assertThrows(NotFoundException.class, () ->
                accountService.getTransactionsByAccountId(testUser.getId(), accountId, 0, 10)
        );
    }
}