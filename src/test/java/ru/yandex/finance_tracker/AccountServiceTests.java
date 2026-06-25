package ru.yandex.finance_tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.mapper.AccountMapper;
import ru.yandex.finance_tracker.mapper.TransactionMapper;
import ru.yandex.finance_tracker.model.*;
import ru.yandex.finance_tracker.security.utils.SecurityUtils;
import ru.yandex.finance_tracker.service.AccountService;
import ru.yandex.finance_tracker.service.AccountServiceImpl;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {
    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    private TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    private AccountService accountService;
    @Mock
    private SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(
                accountRepository,
                userRepository,
                mapper,
                transactionRepository,
                transactionMapper,
                securityUtils
        );
    }

    @ParameterizedTest
    @MethodSource("ru.yandex.finance_tracker.ArgumentsForTests#validAccountRequests")
    void shouldCreateAccount(AccountCreateRequest request) {

        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setEmail("test@mail.com");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = accountService.createAccount(userId, request);

        ArgumentCaptor<Account> captor =
                ArgumentCaptor.forClass(Account.class);

        verify(accountRepository).save(captor.capture());

        Account saved = captor.getValue();

        assertEquals(user, saved.getUser());
        assertEquals(request.getName(), saved.getName());
        assertEquals(request.getInitialBalance(), saved.getBalance());
        assertEquals(request.getCurrency(), saved.getCurrency());

        assertNotNull(result);
    }

    @Test
    void shouldReturnTransactionsForAccount() {

        Long userId = 1L;
        Long accountId = 10L;

        Instant now = Instant.now();

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(BigDecimal.valueOf(1000));

        User user = new User();
        user.setId(userId);

        Transaction tx1 = new Transaction(
                100L,
                account,
                Type.INCOME,
                BigDecimal.valueOf(100),
                Currency.RUB,
                "test",
                now,
                "test123",
                user
        );


        TransactionInfoDto dto1 = new TransactionInfoDto(
                100L,
                accountId,
                Type.INCOME,
                BigDecimal.valueOf(100),
                "test",
                now,
                "test123",
                BigDecimal.valueOf(1000)
        );

        when(accountRepository.existsByIdAndUserId(accountId, userId))
                .thenReturn(true);

        when(transactionRepository.findByAccountId(
                eq(accountId),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(tx1)));


        List<TransactionInfoDto> result =
                accountService.getTransactionsByAccountId(
                        userId,
                        accountId,
                        0,
                        20
                );

        assertEquals(1, result.size());
        assertEquals(dto1, result.getFirst());

        verify(accountRepository)
                .existsByIdAndUserId(accountId, userId);

        verify(transactionRepository)
                .findByAccountId(eq(accountId), any(PageRequest.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountNotBelongToUser() {

        Long userId = 1L;
        Long accountId = 10L;

        when(accountRepository.existsByIdAndUserId(accountId, userId))
                .thenReturn(false);

        assertThrows(
                NotFoundException.class,
                () -> accountService.getTransactionsByAccountId(
                        userId,
                        accountId,
                        0,
                        20
                )
        );

        verify(accountRepository)
                .existsByIdAndUserId(accountId, userId);

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldReturnAccountsForCurrentUser() {
        Long userId = 1L;
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userRepository.existsById(userId)).thenReturn(true);

        List<Account> accounts = List.of(new Account(), new Account());
        when(accountRepository.findByUserId(userId)).thenReturn(accounts);

        List<AccountInfoDto> result = accountService.getAccountsByUserId(userId);

        assertThat(result).hasSize(2);
        verify(accountRepository).findByUserId(userId);
    }

    @Test
    void shouldThrowAccessDeniedWhenRequestingAnotherUserAccounts() {
        Long currentUserId = 1L;
        Long requestedUserId = 2L;

        when(securityUtils.getCurrentUserId()).thenReturn(currentUserId);

        assertThrows(NotFoundException.class,
                () -> accountService.getAccountsByUserId(requestedUserId));
    }

    @Test
    void shouldThrowAccessDeniedWhenAccountDoesNotBelongToUser() {
        Long userId = 1L;
        Long accountId = 999L;

        when(accountRepository.existsByIdAndUserId(accountId, userId))
                .thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> accountService.getTransactionsByAccountId(userId, accountId, 0, 20));

        verify(accountRepository).existsByIdAndUserId(accountId, userId);
        verifyNoInteractions(transactionRepository);
    }
}
