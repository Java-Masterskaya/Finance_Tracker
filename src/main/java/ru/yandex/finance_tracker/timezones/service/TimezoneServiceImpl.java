package ru.yandex.finance_tracker.timezones.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.finance_tracker.exception.InvalidTimezoneException;
import ru.yandex.finance_tracker.exception.NotFoundException;
import ru.yandex.finance_tracker.model.User;
import ru.yandex.finance_tracker.storage.UserRepository;
import ru.yandex.finance_tracker.timezones.dto.TimezoneRequest;
import ru.yandex.finance_tracker.timezones.mapper.GeoApiProperties;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimezoneServiceImpl implements TimezoneService {
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final GeoApiProperties geoApiProperties;

    @Transactional
    public void updateTimezone(Long userId, TimezoneRequest request) {
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
        String ip = getRealClientIp(request);
        return getTimezoneByIp(ip).orElse(ZoneId.systemDefault().getId());
    }

    private String getRealClientIp(HttpServletRequest request) {
        // Заголовок X-Forwarded-For
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String ip = xForwardedFor.split(",")[0].trim();
            if (isValidIp(ip)) {
                log.debug("IP из X-Forwarded-For: {}", ip);
                return ip;
            }
        }

        // Заголовок X-Real-IP
        String xRealIp = request.getHeader("X-Real-IP");
        if (isValidIp(xRealIp)) {
            log.debug("IP из X-Real-IP: {}", xRealIp);
            return xRealIp;
        }

        // Fallback на remoteAddr (может быть IP прокси)
        String remoteAddr = request.getRemoteAddr();
        log.debug("IP из RemoteAddr (fallback): {}", remoteAddr);
        return remoteAddr;
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip);
    }

    private Optional<String> getTimezoneByIp(String ip) {
        if (isLocalIp(ip)) {
            log.debug("Недействительный IP-адрес: {}. Возвращаем системный часовой пояс", ip);
            return Optional.of(ZoneId.systemDefault().getId());
        }

        try {
            String url = String.format(geoApiProperties.getUrl(), ip);
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

    private boolean isLocalIp(String ip) {
        return ip == null || ip.isBlank() ||
                "127.0.0.1".equals(ip) ||
                "0:0:0:0:0:0:0:1".equals(ip) ||
                "localhost".equalsIgnoreCase(ip);
    }
}
