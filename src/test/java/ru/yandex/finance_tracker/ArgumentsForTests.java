package ru.yandex.finance_tracker;

import org.junit.jupiter.params.provider.Arguments;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.model.Account;
import ru.yandex.finance_tracker.model.Currency;
import ru.yandex.finance_tracker.model.Type;

import java.time.LocalDate;
import java.util.stream.Stream;

public class ArgumentsForTests {

    public static Stream<Arguments> currencyMismatchExceptionArguments() {
        return Stream.of(
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                50F,
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.EUR, 100F)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                50F,
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.USD, 100F)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                50F,
                                Currency.EUR,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.RUB, 100F)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                50F,
                                Currency.EUR,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.USD, 100F)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                50F,
                                Currency.USD,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.RUB, 100F)
                ),

                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                50F,
                                Currency.USD,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.EUR, 100F)
                )
        );
    }

    public static Stream<Arguments> argumentsForCreateTransaction() {
        return Stream.of(
                Arguments.of(new TransactionRequest(
                                1L,
                                Type.INCOME,
                                15.5F,
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.RUB, 0F)
                ),
                Arguments.of(new TransactionRequest(
                                1L,
                                Type.EXPENSE,
                                50F,
                                Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        ),
                        new Account(1L, null, "name", Currency.RUB, 100F)
                )
        );
    }

    public static Stream<Arguments> InsufficientBalanceArguments() {

        return Stream.of(
                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, 1f, Currency.RUB, "category", LocalDate.now(), "test"),
                        new Account(1L, null, "name", Currency.RUB, 0f)
                ),

                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, Float.MIN_VALUE * 2, Currency.RUB, "category", LocalDate.now(), "test"),
                        new Account(1L, null, "name", Currency.RUB, Float.MIN_VALUE)
                ),

                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, 100.01f, Currency.RUB, "category", LocalDate.now(), "test"),
                        new Account(1L, null, "name", Currency.RUB, 100f)
                ),

                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, 10f, Currency.RUB, "category", LocalDate.now(), "test"),
                        new Account(1L, null, "name", Currency.RUB, 1f)
                ),

                Arguments.of(
                        new TransactionRequest(1L, Type.EXPENSE, 1.000001f, Currency.RUB, "category", LocalDate.now(), "test"),
                        new Account(1L, null, "name", Currency.RUB, 1f)
                )
        );
    }

    public static Stream<Arguments> validAccountRequests() {
        return Stream.of(
                Arguments.of(
                        new AccountCreateRequest(
                                "name",
                                Currency.EUR,
                                15F)
                ),
                Arguments.of(
                        new AccountCreateRequest(
                                "name",
                                Currency.EUR,
                                14515.540F)
                )
        );
    }

    public static Stream<Arguments> invalidAccountRequests() {
        return Stream.of(
                Arguments.of(
                        new AccountCreateRequest(
                                "",
                                Currency.EUR,
                                15F),
                        1
                ), Arguments.of(
                        new AccountCreateRequest(
                                "  ",
                                Currency.EUR,
                                15F),
                        1
                ), Arguments.of(
                        new AccountCreateRequest(
                                null,
                                null,
                                null),
                        3
                ), Arguments.of(
                        new AccountCreateRequest(
                                "abc",
                                Currency.EUR,
                                -15F),
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
                                15.5F, Currency.RUB,
                                "category",
                                LocalDate.now(),
                                "test"
                        )
                ),
                Arguments.of(
                        new TransactionRequest(
                                2L,
                                Type.EXPENSE,
                                3.04003F, Currency.RUB,
                                "151535",
                                LocalDate.now(),
                                null
                        )));
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
                        8
                ),
                Arguments.of(
                        new TransactionRequest(
                                1L,
                                Type.INCOME,
                                -15.5F,
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
                                15.5F,
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
                                15.5F,
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
                                15.5F,
                                Currency.RUB,
                                null,
                                LocalDate.now(),
                                "test"
                        ),
                        2
                ),
                Arguments.of(
                        new TransactionRequest(
                                -1L,
                                Type.INCOME,
                                15.5F,
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
                                15.5F,
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
