package ru.yandex.finance_tracker;

import org.junit.jupiter.params.provider.Arguments;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

public class ArgumentsForTests {

    public static Stream<Arguments> currencyMismatchExceptionArguments() {
        return Stream.of(
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("50"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.EUR, new BigDecimal("100"), false)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("50"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.USD, new BigDecimal("100"), false)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("50"),
                                Currency.EUR,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.RUB, new BigDecimal("100"), false)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("50"),
                                Currency.EUR,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.USD, new BigDecimal("100"), false)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("50"),
                                Currency.USD,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.RUB, new BigDecimal("100"), false)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("50"),
                                Currency.USD,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.EUR, new BigDecimal("100"), false)
                )
        );
    }

    public static Stream<Arguments> argumentsForCreateTransaction() {
        return Stream.of(
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.INCOME,
                                new BigDecimal("15.5"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(
                                1L,
                                null,
                                "name",
                                Currency.RUB,
                                new BigDecimal("0"),
                                false
                        )
                ),
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("50"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(
                                1L,
                                null,
                                "name",
                                Currency.RUB,
                                new BigDecimal("100"),
                                false
                        )
                )
        );
    }

    public static Stream<Arguments> insufficientBalanceArguments() {

        return Stream.of(
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("1"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(
                                1L,
                                null,
                                "name",
                                Currency.RUB,
                                new BigDecimal("0"),
                                false
                        )
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("0.02"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(
                                1L,
                                null,
                                "name",
                                Currency.RUB,
                                new BigDecimal("0.01"),
                                false
                        )
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("100.01"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(
                                1L,
                                null,
                                "name",
                                Currency.RUB,
                                new BigDecimal("100"),
                                false
                        )
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                new BigDecimal("10"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(
                                1L,
                                null,
                                "name",
                                Currency.RUB,
                                new BigDecimal("1"),
                                false
                        )
                )//,
//
//                Arguments.of(
//                        new TransactionRequest(
//                                1L,
//                                Type.EXPENSE,
//                                new BigDecimal("1.000001"),
//                                Currency.RUB,
//                                "category",
//                                LocalDate.now(),
//                                "test"
//                        ),
//                        new Account(
//                                1L,
//                                null,
//                                "name",
//                                Currency.RUB,
//                                new BigDecimal("1"),
//                                false
//                        )
//                )
        );
    }

    public static Stream<Arguments> validAccountRequests() {
        return Stream.of(
                Arguments.of(
                        new AccountCreateRequest(
                                "name",
                                Currency.EUR,
                                new BigDecimal("15"),
                                false
                        )
                ),
                Arguments.of(
                        new AccountCreateRequest(
                                "name",
                                Currency.EUR,
                                new BigDecimal("14515.540"),
                                false
                        )
                )
        );
    }

    public static Stream<Arguments> invalidAccountRequests() {
        return Stream.of(
                Arguments.of(
                        new AccountCreateRequest(
                                "",
                                Currency.EUR,
                                new BigDecimal("15"),
                                false
                        ),
                        1
                ),
                Arguments.of(
                        new AccountCreateRequest(
                                "  ",
                                Currency.EUR,
                                new BigDecimal("15"),
                                false
                        ),
                        1
                ),
                Arguments.of(
                        new AccountCreateRequest(
                                null,
                                null,
                                null,
                                false
                        ),
                        3
                ),
                Arguments.of(
                        new AccountCreateRequest(
                                "abc",
                                Currency.EUR,
                                new BigDecimal("-15"),
                                false
                        ),
                        1
                )
        );
    }

    public static Stream<Arguments> validTransactionRequests() {
        return Stream.of(
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.INCOME,
                                new BigDecimal("15.5"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        )
                ),
                Arguments.of(
                        new TransactionRequest(
                                2L,
                                Type.EXPENSE,
                                new BigDecimal("3.04003"),
                                Currency.RUB,
                                "151535",
                                LocalDate.now(),
                                null
                        )
                )
        );
    }

    public static Stream<Arguments> invalidTransactionRequests() {
        return Stream.of(
                Arguments.of(
                        new TransactionRequest(
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "test"
                        ),
                        7
                ),
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.INCOME,
                                new BigDecimal("-15.5"),
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        1
                ),
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.INCOME,
                                new BigDecimal("15.5"),
                                Currency.RUB,
                                "",
                                LocalDate.now(),
                                "test"
                        ),
                        1
                ),
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.INCOME,
                                new BigDecimal("15.5"),
                                Currency.RUB,
                                "   ",
                                LocalDate.now(),
                                "test"
                        ),
                        1
                ),
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.INCOME,
                                new BigDecimal("15.5"),
                                Currency.RUB,
                                null,
                                LocalDate.now(),
                                "test"
                        ),
                        1
                ),
                Arguments.of(
                        new TransactionRequest(
                                -1L,
                                Type.INCOME,
                                new BigDecimal("15.5"),
                                Currency.RUB,
                                "test",
                                LocalDate.now(),
                                "test"
                        ),
                        1
                ),
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.INCOME,
                                new BigDecimal("15.5"),
                                Currency.RUB,
                                "test",
                                LocalDate.now().plusDays(1),
                                "test"
                        ),
                        1
                )
        );
    }
}