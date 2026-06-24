package ru.yandex.finance_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
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
import ru.yandex.finance_tracker.dto.input.TransactionUpdateRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        AuthInfo mockAuthInfo = new AuthInfo(1L, "test@mail.ru", UserRole.ROLE_ADMIN);
        auth = new UsernamePasswordAuthenticationToken(mockAuthInfo, null, null);
    }

    @Test
    public void shouldReturn400_WhenInsufficientBalanceExceptionIsThrown() throws Exception {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                1L, Type.EXPENSE, new BigDecimal("150.00"), Currency.RUB, 1L, Instant.now(), "test"
        );

        when(idempotencyService.getCachedResponse(any())).thenReturn(Optional.empty());
        when(idempotencyService.tryLock(any())).thenReturn(true);

        doThrow(new InsufficientBalanceException("Недостаточно средств на счете, овердрафт отключен"))
                .when(transactionService).createTransaction(anyLong(), any(TransactionRequest.class));

        String validUuidKey = UUID.randomUUID().toString();

        mockMvc.perform(post("/v1/transactions")
                        .header("X-Idempotency-Key", validUuidKey)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturn200_WhenUpdateTransactionIsSuccessful() throws Exception {
        TransactionUpdateRequest updateRequest = new TransactionUpdateRequest(
                null, null, null, null, "Updated transaction description"
        );

        TransactionInfoDto responseDto = new TransactionInfoDto(
                100L, 1L, Type.EXPENSE, new BigDecimal("50.00"), "Продукты", Instant.now(), "Updated transaction description", new BigDecimal("450.00")
        );

        when(transactionService.updateTransaction(eq(1L), eq(100L), any(TransactionUpdateRequest.class)))
                .thenReturn(responseDto);

        mockMvc.perform(put("/v1/transactions/100")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated transaction description"))
                .andExpect(jsonPath("$.transactionId").value(100));
    }

    @Test
    public void shouldReturn400_WhenUpdateRequestIsInvalid() throws Exception {
        TransactionUpdateRequest invalidRequest = new TransactionUpdateRequest(
                new BigDecimal("0.00"), null, null, null, "Invalid amount test"
        );

        mockMvc.perform(put("/v1/transactions/100")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}