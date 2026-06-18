package ru.yandex.finance_tracker.timezones.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.yandex.finance_tracker.security.dto.AuthInfo;
import ru.yandex.finance_tracker.timezones.dto.TimezoneRequest;
import ru.yandex.finance_tracker.timezones.service.TimezoneServiceImpl;
import ru.yandex.finance_tracker.timezones.model.Timezone;

import java.util.List;

@RestController
@RequestMapping("v1/users/timezone")
@RequiredArgsConstructor
public class TimezoneController {
    private final TimezoneServiceImpl timezoneService;

    @PutMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addTimezone(@Valid @RequestBody TimezoneRequest request,
                                        @AuthenticationPrincipal AuthInfo authInfo) {
        timezoneService.addTimezone(authInfo.getId(), request);

    }

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getAvailableTimezones() {
        return Timezone.POPULAR_TIMEZONES;
    }

    @GetMapping("/recommended")
    @ResponseStatus(HttpStatus.OK)
    public String getRecommendedTimezone(HttpServletRequest request) {
        return timezoneService.getTimezoneByIpOrDefault(request);
    }
}
