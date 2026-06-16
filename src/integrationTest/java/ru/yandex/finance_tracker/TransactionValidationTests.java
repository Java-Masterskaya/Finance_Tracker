package ru.yandex.finance_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.model.UserRole;
import ru.yandex.finance_tracker.security.service.JwtService;
import ru.yandex.finance_tracker.service.TransactionService;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class TransactionValidationTests extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @MockitoSpyBean
    private TransactionService transactionService;

    @Test
    void shouldReturnConflictWhenRequestWithSameIdempotencyKeyAlreadyInProgress()
            throws Exception {

        User user = userRepository.save(
                new User(
                        null,
                        "test@test.com",
                        "passwordHash",
                        "Test User",
                        UserRole.ROLE_USER,
                        new ArrayList<>(),
                        new ArrayList<>()
                )
        );

        String token = jwtService.generateToken(user.getId());

        CountDownLatch serviceStarted = new CountDownLatch(1);
        CountDownLatch releaseService = new CountDownLatch(1);

        doAnswer(invocation -> {

            serviceStarted.countDown();

            releaseService.await(10, TimeUnit.SECONDS);

            return new TransactionInfoDto(
                    1L,
                    1L,
                    Type.INCOME,
                    100F,
                    "salary",
                    LocalDate.now(),
                    "test",
                    100F
            );

        }).when(transactionService)
                .createTransaction(anyLong(), any());

        String body = """
                {
                  "accountId": 1,
                  "type": "INCOME",
                  "amount": 100,
                  "currency": "RUB",
                  "category": "salary",
                  "date": "2026-06-16",
                  "description": "test"
                }
                """;

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        Future<?> firstRequest =
                executor.submit(() ->
                        mockMvc.perform(
                                post("/v1/transactions")
                                        .header("Authorization", "Bearer " + token)
                                        .header("X-Idempotency-Key", "same-key")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body)
                        ).andReturn()
                );

        assertTrue(
                serviceStarted.await(5, TimeUnit.SECONDS),
                "Transaction service was never called"
        );

        mockMvc.perform(
                        post("/v1/transactions")
                                .header("Authorization", "Bearer " + token)
                                .header("X-Idempotency-Key", "same-key")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isConflict());

        releaseService.countDown();

        firstRequest.get(10, TimeUnit.SECONDS);

        executor.shutdown();
    }
}

