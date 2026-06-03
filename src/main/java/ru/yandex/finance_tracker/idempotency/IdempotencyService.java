package ru.yandex.finance_tracker.idempotency;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.finance_tracker.dto.output.TransactionInfoDto;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class IdempotencyService {

    StringRedisTemplate redisTemplate;
    ObjectMapper objectMapper;
    int processTtlSeconds = 180;
    int resultTtlHours = 12;
    String lockKeyPrefix = "idempotency:process:";
    String resultKeyPrefix = "idempotency:result:";

    public boolean tryLock(String key) {
        String lockKey = lockKeyPrefix + key;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(processTtlSeconds));
        return Boolean.TRUE.equals(locked);
    }

    public void unlock(String key) {
        String lockKey = lockKeyPrefix + key;
        redisTemplate.delete(lockKey);
        log.debug("Unlock for key: {}", key);
    }

    public void cacheResponse(String key, TransactionInfoDto response) {
        String resultKey = resultKeyPrefix + key;
        String json = objectMapper.writeValueAsString(response);
        redisTemplate.opsForValue().set(resultKey, json, Duration.ofHours(resultTtlHours));
        unlock(key);
        log.info("Cached response for key={}, ttl={} hours", key, resultTtlHours);
    }

    public Optional<TransactionInfoDto> getCachedResponse(String key) {
        String json = redisTemplate.opsForValue().get(resultKeyPrefix + key);
        if (json == null) {
            return Optional.empty();
        }
        TransactionInfoDto dto = objectMapper.readValue(json, TransactionInfoDto.class);
        log.debug("Return cached response for key={}", key);
        return Optional.of(dto);
    }
}

