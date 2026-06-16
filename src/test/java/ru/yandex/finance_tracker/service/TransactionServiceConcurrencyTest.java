package ru.yandex.finance_tracker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.exception.InsufficientBalanceException;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class TransactionServiceConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1");

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

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

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        //создаем запрос
        TransactionRequest request = new TransactionRequest(accountId,
                Type.EXPENSE,
                new BigDecimal("30.00"),
                Currency.RUB,
                "test",
                LocalDate.now(),
                "транзакция номер: " + latch);

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
}