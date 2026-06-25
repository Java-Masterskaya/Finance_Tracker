package ru.yandex.finance_tracker.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LargeExpenseProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topics.large-expense:large-expense-alert}")
    private String topic;

    public void send(LargeExpenseAlertEvent event) {
        String correlationId = event.getCorrelationId() != null
                ? event.getCorrelationId()
                : UUID.randomUUID().toString();

        log.info("correlationId={} Отправка события о крупном расходе: userId={}, amount={}",
                correlationId, event.getUserId(), event.getAmount());

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                topic,
                null,
                event.getTimestamp().toEpochMilli(),
                event.getUserId().toString(),
                event
        );

        record.headers().add(new RecordHeader(
                "correlationId",
                correlationId.getBytes(StandardCharsets.UTF_8)
        ));

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("correlationId={} Ошибка отправки в Kafka: {}",
                                correlationId, ex.getMessage());
                    } else {
                        log.debug("correlationId={} Событие отправлено в топик {}",
                                correlationId, topic);
                    }
                });
    }
}
