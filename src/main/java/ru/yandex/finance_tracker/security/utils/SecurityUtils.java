package ru.yandex.finance_tracker.security.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.storage.UserRepository;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        String email = auth.getName();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new SecurityException("User not found"));
    }
}
