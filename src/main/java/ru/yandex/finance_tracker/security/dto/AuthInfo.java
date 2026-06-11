package ru.yandex.finance_tracker.security.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import ru.yandex.finance_tracker.model.UserRole;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthInfo {
    Long id;
    String email;
    UserRole role;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role.toAuthority());
    }
}
