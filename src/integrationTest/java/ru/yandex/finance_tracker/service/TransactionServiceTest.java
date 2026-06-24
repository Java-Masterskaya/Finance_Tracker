package ru.yandex.finance_tracker.service;

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
import ru.yandex.finance_tracker.exception.InsufficientBalanceException;
import ru.yandex.finance_tracker.model.*;
import ru.yandex.finance_tracker.security.service.JwtService;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

    @Test
    public void shouldPreventDoubleSpendingUnderHighConcurrency() throws InterruptedException {
        //берем созданный базовый админ аккаунт
        User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));

        //добавляем ему счет с балансом 100, с отключенным overdraft
        Account account = new Account(null, user, "test", Currency.RUB, new BigDecimal("100.00"), false);
        account = accountRepository.saveAndFlush(account);
        Long accountId = account.getId();

        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);

        @SuppressWarnings("unused")
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        //создаем запрос
        TransactionRequest request = new TransactionRequest(accountId,
                Type.EXPENSE,
                new BigDecimal("30.00"),
                Currency.RUB,
                "test",
                Instant.now(),
                "test");

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    transactionService.createTransaction(user.getId(), request);
                    successCount.incrementAndGet();
                } catch (InsufficientBalanceException e) {
                    failureCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        latch.countDown();
        executor.shutdown();

        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        //сверяем количество подтвержденных транзакций
        Account updatedAccount = accountRepository.findById(accountId).orElseThrow();

        assertThat(successCount.get()).isEqualTo(3);
        assertThat(failureCount.get()).isEqualTo(2);
        assertThat(updatedAccount.getBalance()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    public void shouldThrowException_WhenOverdraftDisabledAndBalanceGoesNegative() {
        User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
        Account account = new Account(null, user, "overdraft-disabled-test", Currency.RUB, new BigDecimal("100.00"), false);
        account = accountRepository.saveAndFlush(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(), Type.EXPENSE, new BigDecimal("150.00"), Currency.RUB, "overdraft-check", Instant.now(), "check"
        );

        assertThrows(InsufficientBalanceException.class, () ->
                transactionService.createTransaction(user.getId(), request)
        );

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    public void shouldAllowNegativeBalance_WhenOverdraftEnabled() {
        User user = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("User not found"));
        Account account = new Account(null, user, "overdraft-enabled-test", Currency.RUB, new BigDecimal("100.00"), true);
        account = accountRepository.saveAndFlush(account);

        TransactionRequest request = new TransactionRequest(
                account.getId(), Type.EXPENSE, new BigDecimal("150.00"), Currency.RUB, "overdraft-check", Instant.now(), "check"
        );

        transactionService.createTransaction(user.getId(), request);

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualTo(new BigDecimal("-50.00"));
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
                  "date": "2026-06-16",
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