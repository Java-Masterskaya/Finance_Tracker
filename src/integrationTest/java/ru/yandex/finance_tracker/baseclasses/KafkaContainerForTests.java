package ru.yandex.finance_tracker.baseclasses;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.KAFKA_CONTAINER;
import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.POSTGRESQL_CONTAINER;

@SpringBootTest
@Testcontainers
public abstract class KafkaContainerForTests {
    @Container
    @ServiceConnection
    static KafkaContainer kafka = KAFKA_CONTAINER;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = POSTGRESQL_CONTAINER;
}
