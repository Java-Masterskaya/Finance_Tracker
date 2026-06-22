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
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.InsufficientBalanceException;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.idempotency.IdempotencyService;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.model.UserRole;
import ru.yandex.finance_tracker.security.dto.AuthInfo;
import ru.yandex.finance_tracker.service.TransactionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    public void shouldReturn400_WhenInsufficientBalanceExceptionIsThrown() throws Exception {
        // Arrange
        TransactionRequest request = new TransactionRequest(
                1L, Type.EXPENSE, new BigDecimal("150.00"), Currency.RUB, "test", LocalDate.now(), "test"
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

    @Test
    public void shouldReturn404_WhenNotFoundExceptionIsThrown() throws Exception {
        TransactionRequest request = new TransactionRequest(
                1L, Type.EXPENSE, new BigDecimal("50.00"), Currency.RUB, "test", LocalDate.now(), "test"
        );

        when(idempotencyService.getCachedResponse(any())).thenReturn(Optional.empty());
        when(idempotencyService.tryLock(any())).thenReturn(true);
        doThrow(new NotFoundException("Счет не найден"))
                .when(transactionService).createTransaction(anyLong(), any(TransactionRequest.class));

        AuthInfo mockAuthInfo = new AuthInfo(1L, "test@mail.ru", UserRole.ROLE_ADMIN);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockAuthInfo, null, null);

        mockMvc.perform(post("/v1/transactions")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn409_WhenIdempotencyKeyLockIsHeld() throws Exception {
        TransactionRequest request = new TransactionRequest(
                1L, Type.INCOME, new BigDecimal("100.00"), Currency.RUB, "test", LocalDate.now(), "test"
        );

        when(idempotencyService.getCachedResponse(any())).thenReturn(Optional.empty());
        when(idempotencyService.tryLock(any())).thenReturn(false);

        AuthInfo mockAuthInfo = new AuthInfo(1L, "test@mail.ru", UserRole.ROLE_ADMIN);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockAuthInfo, null, null);

        mockMvc.perform(post("/v1/transactions")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnCachedResponse_WhenIdempotencyKeyExists() throws Exception {
        TransactionRequest request = new TransactionRequest(
                1L, Type.INCOME, new BigDecimal("100.00"), Currency.RUB, "test", LocalDate.now(), "test"
        );

        TransactionInfoDto cachedResponse = new TransactionInfoDto(1L, 1L, Type.INCOME, BigDecimal.TEN, "test", LocalDate.now(), "test",
                BigDecimal.valueOf(100.00));
        when(idempotencyService.getCachedResponse(any())).thenReturn(Optional.of(cachedResponse));

        AuthInfo mockAuthInfo = new AuthInfo(1L, "test@mail.ru", UserRole.ROLE_ADMIN);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockAuthInfo, null, null);

        mockMvc.perform(post("/v1/transactions")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(1L));
        verify(transactionService, never()).createTransaction(anyLong(), any());
    }

    @Test
    public void shouldReturn500_WhenUnexpectedExceptionIsThrown() throws Exception {
        TransactionRequest request = new TransactionRequest(
                1L, Type.EXPENSE, new BigDecimal("10.00"), Currency.RUB, "test", LocalDate.now(), "test"
        );

        when(idempotencyService.getCachedResponse(any())).thenReturn(Optional.empty());
        when(idempotencyService.tryLock(any())).thenReturn(true);
        doThrow(new RuntimeException("Database connection lost"))
                .when(transactionService).createTransaction(anyLong(), any(TransactionRequest.class));

        AuthInfo mockAuthInfo = new AuthInfo(1L, "test@mail.ru", UserRole.ROLE_ADMIN);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockAuthInfo, null, null);

        mockMvc.perform(post("/v1/transactions")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void shouldReturn201_WhenTransactionCreatedSuccessfully() throws Exception {
        TransactionRequest request = new TransactionRequest(
                1L, Type.INCOME, new BigDecimal("200.00"), Currency.RUB, "Salary", LocalDate.now(), "test"
        );

        TransactionInfoDto response = new TransactionInfoDto(42L, 1L, Type.INCOME, new BigDecimal("200.00"),
                "Salary", LocalDate.now(), "test", new BigDecimal("200.00"));

        when(idempotencyService.getCachedResponse(any())).thenReturn(Optional.empty());
        when(idempotencyService.tryLock(any())).thenReturn(true);
        when(transactionService.createTransaction(anyLong(), any(TransactionRequest.class))).thenReturn(response);

        AuthInfo mockAuthInfo = new AuthInfo(1L, "test@mail.ru", UserRole.ROLE_ADMIN);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockAuthInfo, null, null);

        mockMvc.perform(post("/v1/transactions")
                        .header("X-Idempotency-Key", UUID.randomUUID().toString())
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(42L))
                .andExpect(jsonPath("$.amount").value(200.00));
    }
}