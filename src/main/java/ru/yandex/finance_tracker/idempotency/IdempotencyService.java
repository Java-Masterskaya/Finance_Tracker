package ru.yandex.finance_tracker.idempotency;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdempotencyService {

    StringRedisTemplate redisTemplate;
    static String KEY_PREFIX = "idempotency:";

    public boolean isUniqueRequest(String key) {
        String redisKey = KEY_PREFIX + key;
        Boolean isSet = redisTemplate.opsForValue().setIfAbsent(redisKey, "PROCESSING", Duration.ofMinutes(10));
        return Boolean.TRUE.equals(isSet);
    }

    public void removeKey(String key) {
        redisTemplate.delete(KEY_PREFIX + key);
    }
}

