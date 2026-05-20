package ru.yandex.finance_tracker.security.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.model.UserRole;
import ru.yandex.finance_tracker.storage.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @PostConstruct
    public void createDefaultAdmin() {
        if (userRepository.findByEmailIgnoreCase(adminEmail).isEmpty()) {
            User admin = new User(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    "Admin",
                    UserRole.ROLE_ADMIN
            );
            userRepository.save(admin);
            log.info("Default admin created: {}", adminEmail);
        }
    }
}
