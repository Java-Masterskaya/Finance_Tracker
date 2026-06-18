package ru.yandex.finance_tracker.security.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.security.dto.AuthInfo;
import ru.yandex.finance_tracker.storage.UserRepository;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;

    public User getCurrentUser() {
        Long userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        AuthInfo authInfo = (AuthInfo) auth.getPrincipal();
        return authInfo.getId();
    }
}
