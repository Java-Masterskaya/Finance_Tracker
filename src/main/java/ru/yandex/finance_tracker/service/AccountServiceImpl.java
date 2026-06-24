package ru.yandex.finance_tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.input.AccountUpdateRequest;
import ru.yandex.finance_tracker.dto.output.AccountInfoDto;
import ru.yandex.finance_tracker.dto.output.PagedTransactionResponse;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.mapper.AccountMapper;
import ru.yandex.finance_tracker.mapper.TransactionMapper;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.Transaction;
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
        if (!userRepository.existsById(userId) || !userId.equals(currentUserId)) {
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
    public PagedTransactionResponse getTransactionsByAccountId(Long userId, Long accountId, int page, int size) {
        log.info("Запрос транзакций для аккаунта с ID: {}", accountId);

        accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> {
                    log.error("Ошибка получения транзакций: аккаунт с ID {} не существует," +
                            " удален или не принадлежит пользователю {}", accountId, userId);
                    return new NotFoundException("Account not found or access denied");
                });

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("date").descending());
        Page<Transaction> transactionPage = transactionRepository.findByAccount_IdAndIsDeletedFalse(accountId, pageRequest);

        List<TransactionInfoDto> content = transactionPage.getContent().stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        return PagedTransactionResponse.builder()
                .content(content)
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .build();
    }

    @Transactional
    @Override
    public AccountInfoDto updateAccount(Long userId, Long accountId, AccountUpdateRequest request) {
        log.info("Запрос на изменение счета {}, от пользователя: {}", accountId, userId);

        Account account = accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account not found or access denied"));

        if (request.name() != null && !request.name().isBlank()) {
            log.debug("Изменение названия счета old: {}, new: {}", account.getName(), request.name());
            account.setName(request.name());
        }

        if (request.overdraftAllowed() != null) {
            if (!request.overdraftAllowed() && account.getBalance().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Cannot disable overdraft while account balance is negative");
            }
            log.debug("Изменение возможности кредита на: {}", request.overdraftAllowed());
            account.setOverdraftAllowed(request.overdraftAllowed());
        }

        return mapper.toDto(accountRepository.save(account));
    }

    @Transactional
    @Override
    public void deleteAccount(Long userId, Long accountId) {
        log.info("Запрос на удаление счета {}, от пользователя: {}", accountId, userId);

        Account account = accountRepository.findByIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> new NotFoundException("Account not found or access denied"));

        account.setDeleted(true);
        accountRepository.save(account);
        transactionRepository.softDeleteAllByAccountId(accountId);

        log.info("Счет ID: {} и все его транзакции удален пользователем: {}", accountId, userId);
    }
}
