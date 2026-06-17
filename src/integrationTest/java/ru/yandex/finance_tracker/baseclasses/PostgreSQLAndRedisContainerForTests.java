package ru.yandex.finance_tracker.baseclasses;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.POSTGRESQL_CONTAINER;
import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.REDIS_CONTAINER;

@SpringBootTest
@Testcontainers
public class PostgreSQLAndRedisContainerForTests {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = POSTGRESQL_CONTAINER;

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = REDIS_CONTAINER;
}
