package ru.yandex.finance_tracker;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.finance_tracker.dto.input.AccountCreateRequest;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DTOValidationTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @ParameterizedTest
    @MethodSource("ru.yandex.finance_tracker.ArgumentsForTests#validTransactionRequests")
    void shouldAcceptValidTransactionRequest(TransactionRequest request) {
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty(), violations.toString());
    }

    @ParameterizedTest
    @MethodSource("ru.yandex.finance_tracker.ArgumentsForTests#invalidTransactionRequests")
    void shouldRejectInvalidTransactionRequest(TransactionRequest request, Integer exceptedViolation) {
        var violations = validator.validate(request);
        assertEquals(exceptedViolation, violations.size(), violations.toString());
    }

    @ParameterizedTest
    @MethodSource("ru.yandex.finance_tracker.ArgumentsForTests#validAccountRequests")
    void shouldAcceptValidAccountCreateRequest(AccountCreateRequest request) {
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty(), violations.toString());
    }

    @ParameterizedTest
    @MethodSource("ru.yandex.finance_tracker.ArgumentsForTests#invalidAccountRequests")
    void shouldRejectInvalidAccountCreateRequest(AccountCreateRequest request, Integer exceptedViolation) {
        var violations = validator.validate(request);
        assertEquals(exceptedViolation, violations.size(), violations.toString());
    }

}
