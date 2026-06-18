package ru.yandex.finance_tracker.baseclasses;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import static ru.yandex.finance_tracker.baseclasses.ContainersForTests.KAFKA_CONTAINER;

@Testcontainers
@SpringBootTest
public abstract class PostgresAndKafkaContainerForTests {
    @Container
    @ServiceConnection
    static KafkaContainer kafka = KAFKA_CONTAINER;

}
