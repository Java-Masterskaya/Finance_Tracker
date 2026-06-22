package ru.yandex.finance_tracker.baseclasses;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.REDIS_CONTAINER;

@SpringBootTest
@Testcontainers
public abstract class RedisContainerForTests {
    @Container
    @ServiceConnection
    static GenericContainer<?> redis = REDIS_CONTAINER;
}
