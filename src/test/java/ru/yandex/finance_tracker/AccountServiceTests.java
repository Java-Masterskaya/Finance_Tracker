package ru.yandex.finance_tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.mapper.AccountMapper;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.service.AccountService;
import ru.yandex.finance_tracker.service.AccountServiceImpl;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {
    private final AccountMapper mapper = Mappers.getMapper(AccountMapper.class);
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountServiceImpl(
                accountRepository,
                userRepository,
                mapper
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

}
