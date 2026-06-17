package ru.yandex.finance_tracker.baseclasses;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.KAFKA_CONTAINER;
import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.REDIS_CONTAINER;

@SpringBootTest
@Testcontainers
public abstract class RedisAndKafkaContainerForTests {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = KAFKA_CONTAINER;
    @Container
    @ServiceConnection
    static GenericContainer<?> redis = REDIS_CONTAINER;
}
