package ru.yandex.finance_tracker.security.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.yandex.finance_tracker.security.dto.AuthInfo;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }

        AuthInfo authInfo = (AuthInfo) auth.getPrincipal();
        return authInfo.getId();
    }
}
