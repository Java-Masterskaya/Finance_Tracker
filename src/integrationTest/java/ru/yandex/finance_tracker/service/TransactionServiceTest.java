package ru.yandex.finance_tracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.finance_tracker.baseclasses.ContainersForTests;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.CurrencyMismatchException;
import ru.yandex.finance_tracker.dto.input.TransactionUpdateRequest;
import ru.yandex.finance_tracker.exception.InsufficientBalanceException;
import ru.yandex.finance_tracker.model.*;
import ru.yandex.finance_tracker.security.service.JwtService;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.CategoryRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
public class TransactionServiceTest extends ContainersForTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoSpyBean
    private TransactionService transactionService;

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

        //создаем запрос
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

    @Test
    void shouldCreateIncomeTransaction() {
        User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account(
                null,
                user,
                "Income Test",
                Currency.RUB,
                BigDecimal.valueOf(100),
                false
        );
        account = accountRepository.save(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(),
                Type.INCOME,
                BigDecimal.valueOf(50),
                Currency.RUB,
                "Test2",
                Instant.now(),
                "Test2"
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
        assertTrue(
                account.getBalance().add(request.getAmount())
                        .compareTo(updated.getBalance()) == 0
        );
        assertTrue(transactionRepository.findById(result.transactionId()).isPresent());
    }

    @Test
    void shouldCreateExpenseTransaction() {
        User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));

        Account account = new Account(
                null,
                user,
                "Expense Test",
                Currency.RUB,
                BigDecimal.valueOf(100),
                false
        );
        account = accountRepository.save(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(),
                Type.EXPENSE,
                BigDecimal.valueOf(50),
                Currency.RUB,
                "test",
                Instant.now(),
                "test"
        );

        TransactionInfoDto result = transactionService.createTransaction(
                user.getId(),
                request
        );

        Account updated =
                accountRepository.findById(account.getId())
                        .orElseThrow();

        assertTrue(
                account.getBalance().subtract(request.getAmount())
                        .compareTo(updated.getBalance()) == 0
        );
        assertTrue(transactionRepository.findById(result.transactionId()).isPresent());
    }

    @Test
    void accountBalanceShouldNotChangeWhenCurrencyIsDifferent() {
        User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
        Account account = new Account(
                null,
                user,
                "Currency test",
                Currency.RUB,
                BigDecimal.valueOf(100),
                false
        );

        account = accountRepository.save(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(),
                Type.EXPENSE,
                BigDecimal.valueOf(50),
                Currency.EUR,
                "test",
                Instant.now(),
                "test"
        );
        assertThrows(CurrencyMismatchException.class, () -> transactionService.createTransaction(
                user.getId(),
                request
        ));

        assertTrue(
                account.getBalance()
                        .compareTo(accountRepository.findById(account.getId()).get().getBalance()) == 0
        );
    }

    @Test
    void shouldReturnConflictWhenRequestWithSameIdempotencyKeyAlreadyInProgress() throws Exception {
        Integer transactionCount = transactionRepository.findAll().size();
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.save(new Account(
                null,
                user,
                "Idempotency Test",
                Currency.RUB,
                BigDecimal.valueOf(100),
                false
        ));

        String token = jwtService.generateToken(user.getId());
        UUID idempotencyKey = UUID.randomUUID();

        String body = """
                {
                  "accountId": %d,
                  "type": "INCOME",
                  "amount": 100,
                  "currency": "RUB",
                  "category": "salary",
                  "date": "2026-06-16T10:00:00Z",
                  "description": "test"
                }
                """.formatted(account.getId());

        CountDownLatch serviceEntered = new CountDownLatch(1);
        CountDownLatch allowFinish = new CountDownLatch(1);

        doAnswer(invocation -> {

            serviceEntered.countDown();

            allowFinish.await(10, TimeUnit.SECONDS);

            return invocation.callRealMethod();

        }).when(transactionService).createTransaction(anyLong(), any());


        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<?> first = executor.submit(() ->
                mockMvc.perform(post("/v1/transactions")
                                .header("Authorization", "Bearer " + token)
                                .header("X-Idempotency-Key", idempotencyKey.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                        .andExpect(status().isCreated())
        );

        assertTrue(serviceEntered.await(5, TimeUnit.SECONDS),
                "First request did not enter service");

        mockMvc.perform(post("/v1/transactions")
                        .header("Authorization", "Bearer " + token)
                        .header("X-Idempotency-Key", idempotencyKey.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());

        allowFinish.countDown();

        first.get(10, TimeUnit.SECONDS);

        executor.shutdown();

        List<Transaction> txs = transactionRepository.findAll();
        assertThat(txs).hasSize(transactionCount + 1);

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getBalance())
                .isEqualByComparingTo("200");
    }
}