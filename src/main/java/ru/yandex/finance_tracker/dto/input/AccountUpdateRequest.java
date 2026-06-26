package ru.yandex.finance_tracker.dto.input;

import jakarta.validation.constraints.Size;

public record AccountUpdateRequest(
        @Size(max = 100, message = "Account name must be shorter than 100 symbols")
        String name,
        Boolean overdraftAllowed
) {}