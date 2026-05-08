package ru.yandex.finance_tracker.dto.inner;

import lombok.Data;

@Data
public class GetMonthlyReportParam {
    Integer userId;
    Integer year;
    Integer month;
}
