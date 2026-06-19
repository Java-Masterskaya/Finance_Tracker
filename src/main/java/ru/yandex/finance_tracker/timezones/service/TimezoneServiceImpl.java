package ru.yandex.finance_tracker.timezones.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.finance_tracker.exception.InvalidTimezoneException;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.storage.UserRepository;
import ru.yandex.finance_tracker.timezones.dto.TimezoneRequest;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimezoneServiceImpl implements TimezoneService {
    private static final String GEO_API_URL = "http://ip-api.com/json/%s?fields=timezone";
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public void addTimezone(Long userId, TimezoneRequest request) {
        try {
            ZoneId.of(request.getTimezone());
        } catch (DateTimeException e) {
            throw new InvalidTimezoneException(request.getTimezone());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
        user.setTimezone(request.getTimezone());
        userRepository.save(user);
    }

    public String getTimezoneByIpOrDefault(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        return getTimezoneByIp(ip).orElse("UTC");
    }

    private Optional<String> getTimezoneByIp(String ip) {
        if (ip == null || ip.isBlank() || "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            log.debug("Недействительный IP-адрес: {}. Возвращается пустой результат.", ip);
            return Optional.empty();
        }

        try {
            String url = String.format(GEO_API_URL, ip);
            var response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("timezone")) {
                String timezone = (String) response.get("timezone");
                if (timezone != null && !timezone.isBlank()) {
                    log.debug("По IP-адресу - {} обнаружен часовой пояс: {}", ip, timezone);
                    return Optional.of(timezone);
                }
            }

            log.debug("По IP-адресу - {} часовой пояс не был обнаружен", ip);
            return Optional.empty();

        } catch (ResourceAccessException exception) {
            log.error("Сетевая ошибка при вызове API геолокации для IP-адреса {}: {}", ip, exception.getMessage());
            return Optional.empty();

        } catch (RestClientException exception) {
            log.error("Ошибка ответа от API геолокации для IP-адреса {}: {}", ip, exception.getMessage());
            return Optional.empty();

        } catch (Exception exception) {
            log.error("Неожиданная ошибка при определении часового пояса для IP-адреса {}: ", ip, exception);
            return Optional.empty();
        }
    }
}
