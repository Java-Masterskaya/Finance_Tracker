package ru.yandex.finance_tracker;

import org.junit.jupiter.params.provider.Arguments;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class ArgumentsForTests {

    private static Account createTestAccount(Long id, Currency currency, BigDecimal balance) {
        Account account = new Account();
        account.setId(id);
        account.setName("name");
        account.setCurrency(currency);
        account.setBalance(balance);
        account.setDeleted(false);
        return account;
    }

    public static Stream<Arguments> currencyMismatchExceptionArguments() {
        return Stream.of(
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("50.00"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.EUR, new BigDecimal("100"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("50.00"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.USD, new BigDecimal("100"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("50.00"), Currency.EUR, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("100"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("50.00"), Currency.EUR, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.USD, new BigDecimal("100"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("50.00"), Currency.USD, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("100"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("50.00"), Currency.USD, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.EUR, new BigDecimal("100"))
                )
        );
    }

    public static Stream<Arguments> argumentsForCreateTransaction() {
        return Stream.of(
                Arguments.of(
                        new TransactionRequest(1L, Type.INCOME, new BigDecimal("15.50"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("0"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("50.00"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("100"))
                )
        );
    }

    public static Stream<Arguments> insufficientBalanceArguments() {
        return Stream.of(
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("1.00"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("0"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("0.02"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("0.01"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("100.01"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("100"))
                ),
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("10.00"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("1"))
                ),
                Arguments.of(
                        // Дробная часть нарушает @Digits(fraction = 2) — отлично подходит для проверки неуспешной валидации баланса/формата
                        new TransactionRequest(1L, Type.EXPENSE, new BigDecimal("1.000001"), Currency.RUB, 1L, Instant.now(), "test"),
                        createTestAccount(1L, Currency.RUB, new BigDecimal("1"))
                )
        );
    }

    public static Stream<Arguments> validAccountRequests() {
        return Stream.of(
                Arguments.of(new AccountCreateRequest("name", Currency.EUR, new BigDecimal("15"), false)),
                Arguments.of(new AccountCreateRequest("name", Currency.EUR, new BigDecimal("14515.540"), false))
        );
    }

    public static Stream<Arguments> invalidAccountRequests() {
        return Stream.of(
                Arguments.of(new AccountCreateRequest("", Currency.EUR, new BigDecimal("15"), false), 1),
                Arguments.of(new AccountCreateRequest("  ", Currency.EUR, new BigDecimal("15"), false), 1),
                Arguments.of(new AccountCreateRequest(null, null, null, false), 3),
                Arguments.of(new AccountCreateRequest("abc", Currency.EUR, new BigDecimal("-15"), false), 1)
        );
    }

    public static Stream<Arguments> validTransactionRequests() {
        return Stream.of(
                Arguments.of(new TransactionRequest(1L, Type.INCOME, new BigDecimal("15.50"), Currency.RUB, 1L, Instant.now(), "test")),
                // Округлил 3.04003 до 3.04, чтобы пройти валидацию @Digits fraction = 2
                Arguments.of(new TransactionRequest(2L, Type.EXPENSE, new BigDecimal("3.04"), Currency.RUB, 151535L, Instant.now(), null))
        );
    }

    public static Stream<Arguments> invalidTransactionRequests() {
        return Stream.of(
                // 1. Все поля null (6 стандартных @NotNull + 1 кастомный от @ValidTransaction) -> Итого 7
                Arguments.of(new TransactionRequest(null, null, null, null, null, null, "test"), 7),

                // 2. Отрицательный amount (1 от @DecimalMin + 1 кастомный 'Amount must be positive') -> Итого 2
                Arguments.of(new TransactionRequest(1L, Type.INCOME, new BigDecimal("-15.50"), Currency.RUB, 1L, Instant.now(), "test"), 2),

                // 3. Слишком много знаков после запятой (только 1 от @Digits) -> Итого 1
                Arguments.of(new TransactionRequest(1L, Type.INCOME, new BigDecimal("15.555"), Currency.RUB, 1L, Instant.now(), "test"), 1),

                // 4. Отрицательный id аккаунта (только 1 от @Positive) -> Итого 1
                Arguments.of(new TransactionRequest(-1L, Type.INCOME, new BigDecimal("15.50"), Currency.RUB, 1L, Instant.now(), "test"), 1),

                // 5. Отрицательный id категории (только 1 от @Positive) -> Итого 1
                Arguments.of(new TransactionRequest(1L, Type.INCOME, new BigDecimal("15.50"), Currency.RUB, -1L, Instant.now(), "test"), 1),

                // 6. Дата из будущего -> Итого 1 (если проверяется внутри @ValidTransaction)
                Arguments.of(new TransactionRequest(1L, Type.INCOME, new BigDecimal("15.50"), Currency.RUB, 1L, Instant.now().plus(1, ChronoUnit.DAYS), "test"), 1)
        );
    }
}