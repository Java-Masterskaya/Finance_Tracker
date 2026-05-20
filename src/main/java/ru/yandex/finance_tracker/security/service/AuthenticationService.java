package ru.yandex.finance_tracker.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import ru.yandex.finance_tracker.security.dto.JwtResponse;
import ru.yandex.finance_tracker.security.dto.LoginRequest;
import ru.yandex.finance_tracker.security.dto.RegistrationRequest;

public interface AuthenticationService {
    JwtResponse registration(RegistrationRequest request);

    JwtResponse login(LoginRequest request);

    void checkAuthority(Long userIdFromHeader, UserDetails userDetails);
}
