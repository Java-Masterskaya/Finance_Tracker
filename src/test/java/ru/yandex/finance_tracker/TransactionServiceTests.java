package ru.yandex.finance_tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.CurrencyMismatchException;
import ru.yandex.finance_tracker.exception.InsufficientBalanceException;
import ru.yandex.finance_tracker.mapper.TransactionMapper;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.Transaction;
import ru.yandex.finance_tracker.model.Type;
import ru.yandex.finance_tracker.service.TransactionService;
import ru.yandex.finance_tracker.service.TransactionServiceImpl;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTests {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(
                accountRepository,
                transactionRepository,
                Mappers.getMapper(TransactionMapper.class)
        );
    }

    @ParameterizedTest
    @MethodSource("ru.yandex.finance_tracker.ArgumentsForTests#InsufficientBalanceArguments")
    void shouldThrowInsufficientBalanceException(TransactionRequest request, Account account) {
        when(accountRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(account));
        assertThrows(InsufficientBalanceException.class,
                () -> service.createTransaction(1L, request));

    }

    @ParameterizedTest
    @MethodSource("ru.yandex.finance_tracker.ArgumentsForTests#currencyMismatchExceptionArguments")
    void shouldThrowCurrencyMismatchException(TransactionRequest request, Account account) {
        when(accountRepository.findByIdAndUserId(anyLong(), anyLong())).thenReturn(Optional.of(account));
        assertThrows(CurrencyMismatchException.class, () -> service.createTransaction(1L, request));
    }

    @ParameterizedTest
    @MethodSource("ru.yandex.finance_tracker.ArgumentsForTests#argumentsForCreateTransaction")
    void shouldCreateTransaction(TransactionRequest request, Account account) {
        Float balance = account.getBalance();

        Long userId = 1L;

        when(accountRepository.findByIdAndUserId(
                anyLong(),
                anyLong()
        )).thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TransactionInfoDto result =
                service.createTransaction(userId, request);

        ArgumentCaptor<Account> accountCaptor =
                ArgumentCaptor.forClass(Account.class);

        ArgumentCaptor<Transaction> txCaptor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(accountRepository).save(accountCaptor.capture());
        verify(transactionRepository).save(txCaptor.capture());

        Account savedAccount = accountCaptor.getValue();
        Transaction savedTx = txCaptor.getValue();

        if (request.getType() == Type.INCOME) {
            assertTrue(savedAccount.getBalance() > balance);
        } else {
            assertTrue(savedAccount.getBalance() <= balance);
        }

        assertEquals(account, savedTx.getAccount());
        assertEquals(account.getUser(), savedTx.getUser());

        assertNotNull(result);
    }
}