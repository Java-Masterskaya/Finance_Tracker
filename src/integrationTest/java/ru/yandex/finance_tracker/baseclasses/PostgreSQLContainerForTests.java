package ru.yandex.finance_tracker.baseclasses;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.POSTGRESQL_CONTAINER;

@Testcontainers
@SpringBootTest
public abstract class PostgreSQLContainerForTests {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = POSTGRESQL_CONTAINER;
}
