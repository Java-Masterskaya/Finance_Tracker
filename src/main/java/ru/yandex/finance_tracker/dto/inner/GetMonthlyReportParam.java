package ru.yandex.finance_tracker.dto.inner;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetMonthlyReportParam {
    Integer userId;
    Integer year;
    Integer month;
}
