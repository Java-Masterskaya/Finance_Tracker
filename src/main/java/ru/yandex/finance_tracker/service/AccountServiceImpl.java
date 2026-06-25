package ru.yandex.finance_tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.mapper.AccountMapper;
import ru.yandex.finance_tracker.mapper.TransactionMapper;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.security.utils.SecurityUtils;
import ru.yandex.finance_tracker.storage.AccountRepository;
import ru.yandex.finance_tracker.storage.TransactionRepository;
import ru.yandex.finance_tracker.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper mapper;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    @Override
    public List<AccountInfoDto> getAccountsByUserId(Long userId) {
        log.info("Запрос списка счетов для пользователя с ID: {}", userId);
        Long currentUserId = securityUtils.getCurrentUserId();
        if (!userId.equals(currentUserId)) {
            log.error("Попытка доступа к чужим счетам: userId = {}", userId);
            throw new AccessDeniedException("You can only access your own accounts");
        }

        if (!userRepository.existsById(userId)) {
            log.error("Ошибка получения счетов: пользователь с ID {} не существует", userId);
            throw new NotFoundException("User with ID %d not found".formatted(userId));
        }

        List<Account> accounts = accountRepository.findByUserId(userId);
        log.info("Найдено счетов: {} для пользователя ID: {}", accounts.size(), userId);
        return mapper.toDtoList(accounts);
    }

    @Transactional
    @Override
    public AccountInfoDto createAccount(Long userId, AccountCreateRequest request) {
        log.info("Начало процесса создания счета для пользователя ID: {}. Параметры: {}", userId, request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Не удалось создать счет: пользователь с ID {} не найден", userId);
                    return new NotFoundException("User with ID %d not found".formatted(userId));
                });

        log.debug("Пользователь для привязки счета найден: {}", user.getEmail());

        Account account = mapper.toEntity(request);
        account.setUser(user);

        Account savedAccount = accountRepository.save(account);
        log.info("Счет успешно создан и сохранен. ID нового счета: {}, Владелец ID: {}", savedAccount.getId(), userId);
        return mapper.toDto(savedAccount);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionInfoDto> getTransactionsByAccountId(Long userId, Long accountId, int page, int size) {
        log.info("Запрос транзакций для аккаунта с ID: {} от пользователя ID: {}", accountId, userId);
        if (accountRepository.existsByIdAndUserId(accountId, userId)) {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by("date").descending());
            return transactionRepository
                    .findByAccountId(accountId, pageRequest)
                    .stream()
                    .map(transactionMapper::toResponse)
                    .collect(Collectors.toList());

        } else {
            log.warn("Предупреждение: аккаунт с ID {} не существует или не принадлежит пользователю {}", accountId, userId);
            throw new NotFoundException("Account with ID %d not found".formatted(accountId));
        }
    }
}
