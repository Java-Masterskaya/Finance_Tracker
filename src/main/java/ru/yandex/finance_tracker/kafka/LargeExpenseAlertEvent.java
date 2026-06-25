package ru.yandex.finance_tracker.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LargeExpenseAlertEvent {
    private String correlationId;     // для отслеживания запроса
    private Long userId;
    private Long accountId;
    private BigDecimal amount;
    private String category;
    private Instant timestamp;
}