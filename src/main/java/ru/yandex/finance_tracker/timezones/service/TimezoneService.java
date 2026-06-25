package ru.yandex.finance_tracker.timezones.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.yandex.finance_tracker.timezones.dto.TimezoneRequest;

public interface TimezoneService {
    void updateTimezone(Long userId, TimezoneRequest request);

    String getTimezoneByIpOrDefault(HttpServletRequest request);
}
