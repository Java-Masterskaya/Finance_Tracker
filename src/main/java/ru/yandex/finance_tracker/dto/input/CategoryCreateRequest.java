package ru.yandex.finance_tracker.dto.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
        @NotBlank(message = "Название категории не может быть пустым")
        @Size(max = 50, message = "Название категории не должно превышать 50 символов")
        String name
) {}