package ru.yandex.finance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.exception.InsufficientBalanceException;
import ru.yandex.finance_tracker.idempotency.IdempotencyService;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.model.UserRole;
import ru.yandex.finance_tracker.security.dto.AuthInfo;
import ru.yandex.finance_tracker.service.TransactionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class TransactionControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.1");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private IdempotencyService idempotencyService;

    @Test
    public void shouldReturn400_WhenInsufficientBalanceExceptionIsThrown() throws Exception {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                1L, Type.EXPENSE, new BigDecimal("150.00"), Currency.RUB, "test", Instant.now(), "test"
        );

        when(idempotencyService.getCachedResponse(any())).thenReturn(Optional.empty());
        when(idempotencyService.tryLock(any())).thenReturn(true);

        doThrow(new InsufficientBalanceException("Недостаточно средств на счете, овердрафт отключен"))
                .when(transactionService).createTransaction(anyLong(), any(TransactionRequest.class));

        AuthInfo mockAuthInfo = new AuthInfo(1L, "test@mail.ru", UserRole.ROLE_ADMIN);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                mockAuthInfo, null, null
        );

        String validUuidKey = UUID.randomUUID().toString();

        mockMvc.perform(post("/v1/transactions")
                        .header("X-Idempotency-Key", validUuidKey)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}