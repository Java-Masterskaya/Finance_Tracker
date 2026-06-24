package ru.yandex.finance_tracker.validation.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.finance_tracker.dto.input.TransactionRequest;
import ru.yandex.finance_tracker.validation.validator.ValidTransaction;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionValidator implements ConstraintValidator<ValidTransaction, TransactionRequest> {

    @Override
    public boolean isValid(TransactionRequest transactionRequest,
                           ConstraintValidatorContext constraintValidatorContext) {
        BigDecimal amount = transactionRequest.getAmount();
        Instant date = transactionRequest.getDate();

        return isValidTransactionAmount(amount, constraintValidatorContext)
                && isValidTransactionDate(date, constraintValidatorContext);
    }

    private boolean isValidTransactionAmount(BigDecimal amountRequest,
                                             ConstraintValidatorContext context) {
        if (amountRequest == null) {
            buildValidationError(context,
                    "Amount is missing. Amount must be specified",
                    "amount");
            return false;
        }

        if (amountRequest.compareTo(BigDecimal.ZERO) <= 0) {
            buildValidationError(context,
                    "Amount must be positive",
                    "amount");
            return false;
        }

        return true;
    }

    private boolean isValidTransactionDate(Instant date,
                                           ConstraintValidatorContext context) {
        if (date == null) {
            buildValidationError(context,
                    "Date is missing. Date must be specified",
                    "date");
            return false;
        } else {
            if (date.isAfter(Instant.now())) {
                buildValidationError(context,
                        "The date cannot be a future date",
                        "date");
                return false;
            }
        }

        return true;
    }

    private void buildValidationError(ConstraintValidatorContext context,
                                      String message,
                                      String propertyNode,
                                      Object... args) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(String.format(message, args))
                .addPropertyNode(propertyNode)
                .addConstraintViolation();

    }
}
