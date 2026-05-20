package ru.yandex.finance_tracker.model;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum UserRole {
    ROLE_ADMIN,
    ROLE_USER;

    public SimpleGrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority(this.name());
    }
}
