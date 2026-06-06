package ru.yandex.finance_tracker.validation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.finance_tracker.validation.annotation.TransactionValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TransactionValidator.class)
public @interface ValidTransaction {
    String message() default "Validation error";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
