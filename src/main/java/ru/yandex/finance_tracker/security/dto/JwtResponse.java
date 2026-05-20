package ru.yandex.finance_tracker.security.dto;

import ru.yandex.finance_tracker.model.UserRole;

public record JwtResponse(String token, String email, UserRole role) {
}
