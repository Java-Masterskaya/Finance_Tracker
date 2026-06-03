package ru.yandex.finance_tracker.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.finance_tracker.exception.AccessDeniedException;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.exception.UserAlreadyExistsException;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.model.UserRole;
import ru.yandex.finance_tracker.security.dto.JwtResponse;
import ru.yandex.finance_tracker.security.dto.LoginRequest;
import ru.yandex.finance_tracker.security.dto.RegistrationRequest;
import ru.yandex.finance_tracker.storage.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public JwtResponse registration(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(request.getEmail());
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFirstName(),
                UserRole.ROLE_USER
        );
        userRepository.save(user);
        String token = jwtService.generateToken(user.getId());
        return new JwtResponse(token, user.getEmail(), user.getRole());
    }

    @Override
    public JwtResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new NotFoundException(
                        "User with email = %s was not found".formatted(request.email())
                ));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        String token = jwtService.generateToken(user.getId());
        return new JwtResponse(token, user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    @Override
    public void checkAuthority(Long userIdFromHeader, UserDetails userDetails) {
        String emailFromToken = userDetails.getUsername();
        log.debug("Проверка соответствия почты(токена): {} и заголовка userId: {}", emailFromToken, userIdFromHeader);

        User user = userRepository.findByEmailIgnoreCase(emailFromToken)
                .orElseThrow(() -> {
                    log.error("Проверка авторизации не пройдена: " +
                            "Пользователь с адресом электронной почты {} не найден в базе данных", emailFromToken);
                    return new NotFoundException("User with email = %s was not found".formatted(emailFromToken));
                });

        if (!user.getId().equals(userIdFromHeader)) {
            log.warn("Пользователь (ID: {}, Email: {}) попытался получить доступ к ресурсам пользователя (ID: {})",
                    user.getId(), emailFromToken, userIdFromHeader);
            throw new AccessDeniedException(
                    "Access denied: You do not have permission to access or modify another user's resources");
        }
        log.debug("Авторизация подтверждена для пользователя: {}", user.getId());
    }
}
